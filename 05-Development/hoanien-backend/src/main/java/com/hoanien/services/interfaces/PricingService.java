package com.hoanien.services.interfaces;

import com.hoanien.models.DynamicPricing;

import java.time.LocalDate;

public interface PricingService {
    void triggerGenerate(LocalDate startDate, LocalDate endDate);
    DynamicPricing addRule(Long categoryId, LocalDate startDate, LocalDate endDate, java.math.BigDecimal modifier);
    java.math.BigDecimal getPriceForDate(com.hoanien.models.RoomCategory category, LocalDate date);
    java.math.BigDecimal calculateTotalRoomCharge(com.hoanien.models.RoomCategory category, LocalDate checkIn, LocalDate checkOut);
}
