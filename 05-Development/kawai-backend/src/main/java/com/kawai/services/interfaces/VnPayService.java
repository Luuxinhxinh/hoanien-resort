package com.kawai.services.interfaces;

import java.util.Map;
import com.kawai.models.PaymentTransaction;

public interface VnPayService {
    String createPaymentUrl(Long bookingId, String ipAddress);
    String createPaymentUrlForWalkIn(Long bookingId, String ipAddress);
    String createPaymentUrlForFoodOrder(Long orderId, String ipAddress);
    String createPaymentUrlFromTransaction(PaymentTransaction txn, String ipAddress);
    Map<String, String> verifyIpn(Map<String, String> queryParams);
}
