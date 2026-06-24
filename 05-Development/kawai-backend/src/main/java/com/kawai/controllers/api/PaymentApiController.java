package com.kawai.controllers.api;

import com.kawai.services.interfaces.VnPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentApiController {

    @Autowired
    private VnPayService vnPayService;

    @GetMapping("/vnpay-return")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> queryParams) {
        String rspCode = queryParams.get("vnp_ResponseCode");

        // VNPay IPN cannot reach localhost, so we trigger IPN logic manually here
        try {
            vnPayService.verifyIpn(queryParams);
        } catch (Exception e) {
            // Ignore if already verified or error
        }

        String txnRef = queryParams.get("vnp_TxnRef");
        String redirectUrl;
        if (txnRef != null && txnRef.startsWith("FOOD_")) {
            redirectUrl = "/order-food?payment=" + ("00".equals(rspCode) ? "success" : "failed");
        } else if (txnRef != null && (txnRef.startsWith("TXN-") || txnRef.startsWith("FOLIO_"))) {
            redirectUrl = "/receptionist/folio?payment=" + ("00".equals(rspCode) ? "success" : "failed");
        } else {
            redirectUrl = "/profile/bookings?payment=" + ("00".equals(rspCode) ? "success" : "failed");
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    @GetMapping("/food-order/{orderId}/vnpay")
    public ResponseEntity<?> vnpayFoodOrder(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrlForFoodOrder(orderId, request.getRemoteAddr());
            return ResponseEntity.ok(Map.of("url", paymentUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> queryParams) {
        Map<String, String> result = vnPayService.verifyIpn(queryParams);
        return ResponseEntity.ok(result);
    }
}
