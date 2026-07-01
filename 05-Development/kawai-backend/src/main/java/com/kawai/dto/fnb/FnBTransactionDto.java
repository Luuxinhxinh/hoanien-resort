package com.kawai.dto.fnb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FnBTransactionDto {
    private Long orderId;
    private String tableName;
    private String roomName;
    private String orderType;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private String paymentType;
    private String orderStatus;
}
