package com.kawai.controllers.api;

import com.kawai.services.interfaces.VnPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

        String redirectUrl = "/profile/bookings?payment=" + ("00".equals(rspCode) ? "success" : "failed");
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> queryParams) {
        Map<String, String> result = vnPayService.verifyIpn(queryParams);
        return ResponseEntity.ok(result);
    }
}
