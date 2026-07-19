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
                final LocalDate targetDate = date;
                BigDecimal finalPrice = cat.getBasePrice() != null ? cat.getBasePrice() : BigDecimal.ZERO;
                
                // Apply rules
                for (DynamicPricing rule : rules) {
                    if (!date.isBefore(rule.getStartDate()) && !date.isAfter(rule.getEndDate())) {
                        finalPrice = finalPrice.add(rule.getPriceModifier());
                    }
                }

                DailyRate rate = dailyRateRepository.findByCategoryIdAndRateDate(cat.getId(), targetDate)
                        .orElseGet(() -> {
                            DailyRate r = new DailyRate();
                            r.setCategory(cat);
                            r.setRateDate(targetDate);
                            return r;
                        });
                rate.setComputedPrice(finalPrice);
                java.time.DayOfWeek dw = date.getDayOfWeek();
                rate.setIsWeekend(dw == java.time.DayOfWeek.SATURDAY || dw == java.time.DayOfWeek.SUNDAY);
                rate.setIsHoliday(false);
                
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
        
        DynamicPricing saved = dynamicPricingRepository.save(pricing);
        
        // Auto trigger generation for the date range of the new rule
        triggerGenerate(startDate, endDate);
        
        return saved;
    }

    @Override
    public BigDecimal getPriceForDate(RoomCategory category, LocalDate date) {
        if (category == null) return BigDecimal.ZERO;
        return dailyRateRepository.findByCategoryIdAndRateDate(category.getId(), date)
                .map(DailyRate::getComputedPrice)
                .orElseGet(() -> {
                    BigDecimal finalPrice = category.getBasePrice() != null ? category.getBasePrice() : BigDecimal.ZERO;
                    List<DynamicPricing> rules = dynamicPricingRepository.findByCategoryId(category.getId());
                    for (DynamicPricing rule : rules) {
                        if (!date.isBefore(rule.getStartDate()) && !date.isAfter(rule.getEndDate())) {
                            finalPrice = finalPrice.add(rule.getPriceModifier());
                        }
                    }
                    return finalPrice;
                });
    }

    @Override
    public BigDecimal calculateTotalRoomCharge(RoomCategory category, LocalDate checkIn, LocalDate checkOut) {
        if (category == null || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return BigDecimal.ZERO;
        }
        
        List<DailyRate> rates = dailyRateRepository.findByCategoryIdAndRateDateBetween(category.getId(), checkIn, checkOut.minusDays(1));
        java.util.Map<LocalDate, BigDecimal> rateMap = new java.util.HashMap<>();
        for (DailyRate r : rates) {
            rateMap.put(r.getRateDate(), r.getComputedPrice());
        }

        BigDecimal total = BigDecimal.ZERO;
        List<DynamicPricing> rules = null;

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            BigDecimal price = rateMap.get(date);
            if (price == null) {
                price = category.getBasePrice() != null ? category.getBasePrice() : BigDecimal.ZERO;
                if (rules == null) {
                    rules = dynamicPricingRepository.findByCategoryId(category.getId());
                }
                for (DynamicPricing rule : rules) {
                    if (!date.isBefore(rule.getStartDate()) && !date.isAfter(rule.getEndDate())) {
                        price = price.add(rule.getPriceModifier());
                    }
                }
            }
            total = total.add(price);
        }
        return total;
    }
}
