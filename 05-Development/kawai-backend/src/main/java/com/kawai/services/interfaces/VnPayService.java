package com.kawai.services.interfaces;

import java.util.Map;

public interface VnPayService {
    String createPaymentUrl(Long bookingId, String ipAddress);
    String createPaymentUrlForFoodOrder(Long orderId, String ipAddress);
    Map<String, String> verifyIpn(Map<String, String> queryParams);
}
