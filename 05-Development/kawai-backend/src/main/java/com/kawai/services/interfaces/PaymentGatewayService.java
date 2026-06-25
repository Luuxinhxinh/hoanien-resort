package com.kawai.services.interfaces;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    /**
     * Hoàn tiền cho giao dịch đã thanh toán.
     * 
     * @param transactionId Mã giao dịch gốc
     * @param amount        Số tiền hoàn
     */
    void processRefund(String transactionId, BigDecimal amount);
}
