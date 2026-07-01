package com.kawai.dto.fnb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FnBDailyReportPreviewResponse {
    private LocalDate reportDate;
    private String staffName;

    private Integer totalDineInOrders;
    private Integer totalRoomServiceOrders;

    private BigDecimal totalCashRevenue;
    private BigDecimal totalVnpayRevenue;
    private BigDecimal totalChargeToRoomRevenue;
    private BigDecimal totalRevenue;

    private List<FnBTransactionDto> transactions;
}
