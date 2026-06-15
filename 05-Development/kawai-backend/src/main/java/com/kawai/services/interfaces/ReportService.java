package com.kawai.services.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface ReportService {
    Map<String, BigDecimal> getUsaliRevenueReport(LocalDate startDate, LocalDate endDate);
    double getOccupancyRate(LocalDate date);
    long getGuestCount(LocalDate date);
    byte[] exportUsaliReport(LocalDate startDate, LocalDate endDate, String format);
}
