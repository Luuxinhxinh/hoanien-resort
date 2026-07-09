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

    @Autowired
    private com.kawai.repositories.PaymentTransactionRepository paymentTransactionRepository;

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
        boolean isSuccess = "00".equals(rspCode);

        // Mặc định redirect về lịch sử booking (cho trường hợp thành công)
        String redirectUrl = "/profile/bookings?payment=success";

        // DEBUG: log để kiểm tra
        System.err.println("[VNPay Return] rspCode=" + rspCode + " | txnRef=" + txnRef);

        if (txnRef != null && txnRef.startsWith("FOOD_")) {
            if (txnRef.endsWith("_PROFILE")) {
                redirectUrl = "/profile/bookings?payment=" + (isSuccess ? "success" : "failed");
            } else {
                redirectUrl = "/order-food?payment=" + (isSuccess ? "success" : "failed");
            }
} else if (txnRef != null && (txnRef.startsWith("FOLIO_") || txnRef.startsWith("TXN-"))) {
            // Nếu là mã TXN- hoặc mã FOLIO_ không đúng định dạng bóc tách, mặc định về trang danh sách folio
            redirectUrl = "/receptionist/folio?payment=" + (isSuccess ? "success" : "failed");

            // Nếu là mã FOLIO_, tiến hành bóc tách nâng cao để đưa về trang chi tiết (detail)
            if (txnRef.startsWith("FOLIO_")) {
                String[] parts = txnRef.split("_");
                String detailId = null;
                
                if (txnRef.startsWith("FOLIO_GROUP_") && parts.length >= 3) {
                    detailId = parts[2];
                } else if (parts.length >= 2) {
                    detailId = parts[1];
                }
                
                if (detailId != null) {
                    redirectUrl = "/receptionist/folio/detail?id=" + detailId + "&payment=" + (isSuccess ? "success" : "failed");
                }
            }
        } else if (txnRef != null && txnRef.startsWith("TOUR_")) {
            redirectUrl = "/tours/detail?payment=" + ("00".equals(rspCode) ? "success" : "failed");
            System.err.println("[VNPay Return] -> Redirecting to tour-detail (TOUR_ prefix)");
        } else if (txnRef != null && (txnRef.startsWith("TXN-") || txnRef.startsWith("FOLIO_"))) {
            redirectUrl = "/receptionist/folio?payment=" + ("00".equals(rspCode) ? "success" : "failed");

        } else if (txnRef != null && txnRef.startsWith("WALKIN_")) {
            redirectUrl = "/receptionist/in-house?payment=" + (isSuccess ? "success" : "failed");
            System.err.println("[VNPay Return] -> Redirecting to in-house (WALKIN_ prefix)");
        } else if (txnRef != null && txnRef.startsWith("CREDITDEPOSIT_")) {
            // Nâng hạn mức tín dụng do lễ tân thực hiện → redirect về trang In-House
            redirectUrl = "/receptionist/in-house?payment=" + (isSuccess ? "success" : "failed");
            System.err.println("[VNPay Return] -> Redirecting to in-house (CREDITDEPOSIT_ prefix)");
        } else {
            // Đặt phòng thông thường (Customer Online Booking)
            if (isSuccess) {
                // Thành công → lịch sử booking
                redirectUrl = "/profile/bookings?payment=success";
            } else {
                // Thất bại → quay lại form payment để khách retry (không cần chọn phòng lại)
                String fallbackBId = queryParams.get("bId");
                String bookingIdStr = fallbackBId;

                if (bookingIdStr == null && txnRef != null) {
                    String[] parts = txnRef.split("_");
                    if (parts.length > 0) {
                        bookingIdStr = parts[0];
                    }
                }

                if (bookingIdStr != null && bookingIdStr.matches("\\d+")) {
                    redirectUrl = "/payment?bookingId=" + bookingIdStr + "&payment=failed";
                } else {
                    // Fallback nếu không parse được bookingId
                    redirectUrl = "/profile/bookings?payment=failed";
                }
            }
            System.err.println("[VNPay Return] -> Customer Booking | isSuccess=" + isSuccess + " | bookingId="
                    + (queryParams.get("bId") != null ? queryParams.get("bId") : "null"));
        }

        System.err.println("[VNPay Return] -> Final redirectUrl=" + redirectUrl);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    @GetMapping("/food-order/{orderId}/vnpay")
    public ResponseEntity<?> vnpayFoodOrder(@PathVariable Long orderId,
            @RequestParam(required = false) String from,
            HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrlForFoodOrder(orderId, request.getRemoteAddr(), from);
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
