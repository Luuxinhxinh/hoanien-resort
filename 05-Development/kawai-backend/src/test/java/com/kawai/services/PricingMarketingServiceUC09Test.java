package com.kawai.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawai.models.DailyRate;
import com.kawai.models.DynamicPricing;
import com.kawai.models.Promotion;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.DailyRateRepository;
import com.kawai.repositories.DynamicPricingRepository;
import com.kawai.repositories.PromotionRepository;
import com.kawai.repositories.RoomCategoryRepository;
import com.kawai.services.impl.MarketingServiceImpl;
import com.kawai.services.impl.PricingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC09 - Pricing & Marketing (Daily Rates & JSON Combo)")
public class PricingMarketingServiceUC09Test {

    @Mock
    private RoomCategoryRepository categoryRepository;

    @Mock
    private DynamicPricingRepository dynamicPricingRepository;

    @Mock
    private DailyRateRepository dailyRateRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PricingServiceImpl pricingService;

    @InjectMocks
    private MarketingServiceImpl marketingService;

    @Test
    @DisplayName("TC-UC09-001 | Thuật toán Generate Daily Rates tính toán chính xác")
    void testTriggerGenerate_Success() {
        RoomCategory category = new RoomCategory();
        category.setId(1L);
        category.setBasePrice(new BigDecimal("1000"));

        DynamicPricing rule = new DynamicPricing();
        rule.setStartDate(LocalDate.of(2026, 6, 1));
        rule.setEndDate(LocalDate.of(2026, 6, 5));
        rule.setPriceModifier(new BigDecimal("200"));

        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(category));
        when(dynamicPricingRepository.findByCategoryId(1L)).thenReturn(Collections.singletonList(rule));

        // Generate from 3 to 6
        LocalDate start = LocalDate.of(2026, 6, 3);
        LocalDate end = LocalDate.of(2026, 6, 6);

        pricingService.triggerGenerate(start, end);

        ArgumentCaptor<DailyRate> rateCaptor = ArgumentCaptor.forClass(DailyRate.class);
        verify(dailyRateRepository, times(4)).save(rateCaptor.capture());

        List<DailyRate> capturedRates = rateCaptor.getAllValues();
        assertEquals(4, capturedRates.size());

        // Day 3 -> 1200
        DailyRate day3 = capturedRates.stream().filter(r -> r.getRateDate().equals(LocalDate.of(2026, 6, 3))).findFirst().get();
        assertEquals(new BigDecimal("1200"), day3.getComputedPrice());

        // Day 6 -> 1000
        DailyRate day6 = capturedRates.stream().filter(r -> r.getRateDate().equals(LocalDate.of(2026, 6, 6))).findFirst().get();
        assertEquals(new BigDecimal("1000"), day6.getComputedPrice());
    }

    @Test
    @DisplayName("TC-UC09-002 | Chặn Rule Giá Động bị giao nhau (Overlapping Dates)")
    void testAddRule_OverlappingDates_ThrowsException() {
        RoomCategory category = new RoomCategory();
        category.setId(1L);

        DynamicPricing existingRule = new DynamicPricing();
        existingRule.setStartDate(LocalDate.of(2026, 6, 1));
        existingRule.setEndDate(LocalDate.of(2026, 6, 10));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(dynamicPricingRepository.findByCategoryId(1L)).thenReturn(Collections.singletonList(existingRule));

        assertThrows(IllegalStateException.class, () -> {
            pricingService.addRule(1L, LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 15), new BigDecimal("100"));
        });
    }

    @Test
    @DisplayName("TC-UC09-003 | Đóng gói Combo JSON hợp lệ")
    void testCreateCombo_JSONSerialization_Success() {
        Map<String, Object> config = new HashMap<>();
        config.put("roomId", 1);
        config.put("tourId", 2);

        Promotion savedPromo = new Promotion();
        savedPromo.setComboConfig("{\"roomId\":1,\"tourId\":2}");

        when(promotionRepository.save(any(Promotion.class))).thenReturn(savedPromo);

        Promotion result = marketingService.createCombo("SUMMER_COMBO", config);

        assertNotNull(result);
        assertEquals("{\"roomId\":1,\"tourId\":2}", result.getComboConfig());
        verify(promotionRepository, times(1)).save(any(Promotion.class));
    }
}
