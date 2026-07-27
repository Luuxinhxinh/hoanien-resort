package com.hoanien.controllers.api;

import com.hoanien.dto.walkin.WalkInCheckInRequest;
import com.hoanien.dto.walkin.WalkInCheckInResponse;
import com.hoanien.dto.walkin.WalkInSurchargeResponse;
import com.hoanien.services.interfaces.WalkInCheckInService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receptionist/walkin")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('OP_RECEPTION_WALKIN', 'OP_RECEPTION_CHECKIN', 'ROLE_ADMIN')")
public class WalkInCheckInApiController {

    public static final String PAYMENT_METHOD_VNPAY = "Chuyển khoản";

    private final WalkInCheckInService walkInCheckInService;
    private final com.hoanien.services.interfaces.VnPayService vnPayService;

    public WalkInCheckInApiController(WalkInCheckInService walkInCheckInService,
            com.hoanien.services.interfaces.VnPayService vnPayService) {
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

    @GetMapping("/search-customer")
    public ResponseEntity<?> searchCustomer(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String cccd) {
        String searchPhone = (phone != null && !phone.isBlank()) ? phone : keyword;
        String searchCccd = (cccd != null && !cccd.isBlank()) ? cccd : null;

        if ((searchPhone == null || searchPhone.isBlank()) && (searchCccd == null || searchCccd.isBlank())) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Vui lòng nhập SĐT hoặc CCCD để tìm kiếm"));
        }

        java.util.Optional<com.hoanien.models.Customer> customerOpt = walkInCheckInService
                .searchCustomerByPhoneOrCccd(searchPhone, searchCccd);

        if (customerOpt.isPresent()) {
            com.hoanien.models.Customer c = customerOpt.get();
            String decryptedCccd = "";
            if (c.getCccdPassportEncrypted() != null) {
                try {
                    decryptedCccd = com.hoanien.utils.EncryptionUtils.decrypt(c.getCccdPassportEncrypted());
                } catch (Exception e) {
                    decryptedCccd = c.getCccdPassportEncrypted();
                }
            }
            return ResponseEntity.ok(java.util.Map.of(
                    "id", c.getId(),
                    "fullName", c.getFullName() != null ? c.getFullName() : "",
                    "phone", c.getPhone() != null ? c.getPhone() : "",
                    "email", c.getEmail() != null ? c.getEmail() : "",
                    "cccd", decryptedCccd,
                    "dateOfBirth", c.getBirthDate() != null ? c.getBirthDate().toString() : "",
                    "gender", c.getGender() != null ? c.getGender() : ""));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("message", "Không tìm thấy khách hàng"));
    }
}
