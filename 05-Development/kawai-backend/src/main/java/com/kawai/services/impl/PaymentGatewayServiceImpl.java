package com.kawai.services.impl;

import com.kawai.services.interfaces.PaymentGatewayService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    @Override
    public void processRefund(String transactionId, BigDecimal amount) {
        // Dummy implementation
    }
}
