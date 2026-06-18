package com.kawai.services.impl;

import com.kawai.models.DailyRate;
import com.kawai.models.DynamicPricing;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.DailyRateRepository;
import com.kawai.repositories.DynamicPricingRepository;
import com.kawai.repositories.RoomCategoryRepository;
import com.kawai.services.interfaces.PricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PricingServiceImpl implements PricingService {

    private final RoomCategoryRepository categoryRepository;
    private final DynamicPricingRepository dynamicPricingRepository;
    private final DailyRateRepository dailyRateRepository;

    public PricingServiceImpl(RoomCategoryRepository categoryRepository,
                              DynamicPricingRepository dynamicPricingRepository,
                              DailyRateRepository dailyRateRepository) {
        this.categoryRepository = categoryRepository;
        this.dynamicPricingRepository = dynamicPricingRepository;
        this.dailyRateRepository = dailyRateRepository;
    }

    @Override
    @Transactional
    public void triggerGenerate(LocalDate startDate, LocalDate endDate) {
        List<RoomCategory> categories = categoryRepository.findAll();
        for (RoomCategory cat : categories) {
            List<DynamicPricing> rules = dynamicPricingRepository.findByCategoryId(cat.getId());
            
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                BigDecimal finalPrice = cat.getBasePrice();
                
                // Apply rules
                for (DynamicPricing rule : rules) {
                    if (!date.isBefore(rule.getStartDate()) && !date.isAfter(rule.getEndDate())) {
                        finalPrice = finalPrice.add(rule.getPriceModifier());
                    }
                }

                DailyRate rate = new DailyRate();
                rate.setCategory(cat);
                rate.setRateDate(date);
                rate.setComputedPrice(finalPrice);
                
                // Simplified Upsert Logic
                dailyRateRepository.save(rate);
            }
        }
    }

    @Override
    @Transactional
    public DynamicPricing addRule(Long categoryId, LocalDate startDate, LocalDate endDate, BigDecimal modifier) {
        RoomCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        List<DynamicPricing> existingRules = dynamicPricingRepository.findByCategoryId(categoryId);
        for (DynamicPricing rule : existingRules) {
            // Check overlap
            boolean isOverlap = (startDate.isBefore(rule.getEndDate()) || startDate.equals(rule.getEndDate()))
                    && (endDate.isAfter(rule.getStartDate()) || endDate.equals(rule.getStartDate()));
            if (isOverlap) {
                throw new IllegalStateException("OverlappingDateException: Rule dates conflict with existing rule.");
            }
        }

        DynamicPricing pricing = new DynamicPricing();
        pricing.setCategory(cat);
        pricing.setStartDate(startDate);
        pricing.setEndDate(endDate);
        pricing.setPriceModifier(modifier);
        return dynamicPricingRepository.save(pricing);
    }
}
