package com.kawai.controllers.api;

import com.kawai.dto.walkin.WalkInCheckInRequest;
import com.kawai.dto.walkin.WalkInCheckInResponse;
import com.kawai.dto.walkin.WalkInSurchargeResponse;
import com.kawai.services.interfaces.WalkInCheckInService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receptionist/walkin")
public class WalkInCheckInApiController {

    public static final String PAYMENT_METHOD_VNPAY = "Chuyển khoản";

    private final WalkInCheckInService walkInCheckInService;
    private final com.kawai.services.interfaces.VnPayService vnPayService;

    public WalkInCheckInApiController(WalkInCheckInService walkInCheckInService,
            com.kawai.services.interfaces.VnPayService vnPayService) {
        this.walkInCheckInService = walkInCheckInService;
        this.vnPayService = vnPayService;
    }

    @PostMapping("/calculate-surcharge")
    public ResponseEntity<WalkInSurchargeResponse> calculateSurchargePreview(
            @RequestBody WalkInCheckInRequest request) {
        WalkInSurchargeResponse response = walkInCheckInService.calculateSurchargePreview(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel-pending/{bookingId}")
    public ResponseEntity<?> cancelPendingWalkIn(@PathVariable Long bookingId) {
        walkInCheckInService.cancelPendingWalkIn(bookingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkin")
    public ResponseEntity<WalkInCheckInResponse> walkInCheckIn(
            @RequestBody WalkInCheckInRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        WalkInCheckInResponse response = walkInCheckInService.createWalkInBookingAndCheckIn(request);

        if (PAYMENT_METHOD_VNPAY.equals(request.getPaymentMethod()) && request.getDepositAmount() != null
                && request.getDepositAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            String paymentUrl = vnPayService.createPaymentUrlForWalkIn(response.getBookingId(),
                    httpRequest.getRemoteAddr());
            response.setPaymentUrl(paymentUrl);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API 3: Tìm kiếm khách hàng theo SĐT hoặc CCCD
     */
    @GetMapping("/search-customer")
    public ResponseEntity<?> searchCustomer(@RequestParam(required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Vui lòng nhập từ khóa tìm kiếm (SĐT hoặc CCCD)"));
        }
        java.util.Optional<com.kawai.models.Customer> customerOpt = walkInCheckInService.searchCustomer(keyword);
        if (customerOpt.isPresent()) {
            com.kawai.models.Customer c = customerOpt.get();
            String cccd = "";
            if (c.getCccdPassportEncrypted() != null) {
                try {
                    cccd = com.kawai.utils.EncryptionUtils.decrypt(c.getCccdPassportEncrypted());
                } catch (Exception e) {
                    cccd = c.getCccdPassportEncrypted();
                }
            }
            return ResponseEntity.ok(java.util.Map.of(
                    "id", c.getId(),
                    "fullName", c.getFullName() != null ? c.getFullName() : "",
                    "phone", c.getPhone() != null ? c.getPhone() : "",
                    "email", c.getEmail() != null ? c.getEmail() : "",
                    "cccd", cccd));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("message", "Không tìm thấy khách hàng"));
    }
}
