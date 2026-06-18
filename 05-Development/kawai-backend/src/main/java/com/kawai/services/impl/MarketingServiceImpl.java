package com.kawai.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.Promotion;
import com.kawai.repositories.PromotionRepository;
import com.kawai.services.interfaces.MarketingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MarketingServiceImpl implements MarketingService {

    private final PromotionRepository promotionRepository;
    private final ObjectMapper objectMapper;

    public MarketingServiceImpl(PromotionRepository promotionRepository, ObjectMapper objectMapper) {
        this.promotionRepository = promotionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Promotion createCombo(String promoCode, Map<String, Object> comboData) {
        Promotion promo = new Promotion();
        promo.setPromoCode(promoCode);
        promo.setDiscountType("COMBO_PACKAGE");
        promo.setDiscountValue(new BigDecimal("0"));
        promo.setMaxUses(100);
        promo.setValidFrom(LocalDateTime.now());
        promo.setValidTo(LocalDate.now().plusMonths(1));
        
        try {
            String jsonConfig = objectMapper.writeValueAsString(comboData);
            promo.setComboConfig(jsonConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to stringify combo config", e);
        }

        return promotionRepository.save(promo);
    }
}
