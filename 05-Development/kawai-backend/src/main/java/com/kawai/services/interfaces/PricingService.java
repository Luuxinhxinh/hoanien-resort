package com.kawai.services.interfaces;

import com.kawai.models.DynamicPricing;

import java.time.LocalDate;

public interface PricingService {
    void triggerGenerate(LocalDate startDate, LocalDate endDate);
    DynamicPricing addRule(Long categoryId, LocalDate startDate, LocalDate endDate, java.math.BigDecimal modifier);
    java.math.BigDecimal getPriceForDate(com.kawai.models.RoomCategory category, LocalDate date);
    java.math.BigDecimal calculateTotalRoomCharge(com.kawai.models.RoomCategory category, LocalDate checkIn, LocalDate checkOut);
}
