package com.kawai.controllers.api;

import com.kawai.dto.BookingRequestDTO;
import com.kawai.dto.BookingResponseDTO;
import com.kawai.dto.BookingApiResponse;
import com.kawai.dto.BookingDetailResponseDTO;
import com.kawai.models.Customer;
import com.kawai.repositories.CustomerRepository;
import com.kawai.services.interfaces.BookingService;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.RoomBooking;
import com.kawai.models.PaymentTransaction;
import com.kawai.models.PaymentStatus;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private static final Logger log = LoggerFactory.getLogger(BookingApiController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoomBookingRepository roomBookingRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private com.kawai.services.interfaces.VnPayService vnPayService;

    @PostMapping
    public ResponseEntity<?> createBooking(Principal principal, @RequestBody BookingRequestDTO request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new BookingApiResponse(
                    "error", null, null, null, "Quý khách cần đăng nhập để thực hiện đặt phòng!"));
        }

        try {
            String username = principal.getName();
            Customer customer = customerRepository.findByAccount_Username(username)
                    .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                            "Không tìm thấy thông tin khách hàng cho tài khoản: " + username));

            // Gán customerId lấy từ user đang đăng nhập
            request.setCustomerId(customer.getId());

            BookingResponseDTO response = bookingService.createBooking(request);

            java.time.LocalDateTime cancellationDeadlineLDT = response.getCancellationDeadline() != null
                    ? response.getCancellationDeadline().atStartOfDay()
                    : null;

            BookingApiResponse apiResponse = new BookingApiResponse(
                    "success",
                    response.getBookingStatus(),
                    response.getBookingId(),
                    response.getDepositAmount(),
                    cancellationDeadlineLDT,
                    "Đặt phòng thành công!");
            // ✅ Trả về tổng chính thức từ backend (bao gồm phụ thu + khuyến mãi)
            apiResponse.setDiscountedPrice(response.getDiscountedPrice());

            // TẠO URL THANH TOÁN VNPAY VÀ TRẢ VỀ CHO FRONTEND
            if (response.getBookingId() != null) {
                String paymentUrl = vnPayService.createPaymentUrl(response.getBookingId(), httpRequest.getRemoteAddr());
                apiResponse.setPaymentUrl(paymentUrl);
            }

            return ResponseEntity.ok(apiResponse);
        } catch (BusinessException e) {
            return ResponseEntity.status(400).body(new BookingApiResponse(
                    "error", null, null, null, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(new BookingApiResponse(
                    "error", null, null, null,
                    e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định"));
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(Principal principal, @PathVariable Long bookingId) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new BookingApiResponse(
                    "error", null, null, null, "Quý khách cần đăng nhập để thực hiện thao tác này!"));
        }

        try {
            String username = principal.getName();
            Customer customer = customerRepository.findByAccount_Username(username)
                    .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                            "Không tìm thấy thông tin khách hàng cho tài khoản: " + username));

            BookingResponseDTO response = bookingService.cancelBooking(bookingId, customer.getId());

            String msg = (response.getDepositAmount() != null
                    && response.getDepositAmount().compareTo(BigDecimal.ZERO) > 0)
                            ? "Hủy phòng thành công! Tiền cọc sẽ được hoàn lại."
                            : "Hủy phòng thành công! Rất tiếc, bạn không được hoàn lại tiền do hủy quá sát ngày nhận phòng (< 48h).";

            return ResponseEntity.ok(new BookingApiResponse(
                    "success",
                    response.getBookingStatus(),
                    response.getBookingId(),
                    response.getDepositAmount(),
                    null,
                    msg));
        } catch (com.kawai.exceptions.PaymentGatewayException e) {
            return ResponseEntity.status(500).body(new BookingApiResponse(
                    "error", null, null, null, e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.status(400).body(new BookingApiResponse(
                    "error", null, null, null, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new BookingApiResponse(
                    "error", null, null, null,
                    e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định"));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetail(Principal principal, @PathVariable Long bookingId) {
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Quý khách cần đăng nhập để thực hiện thao tác này!"));
        }
        try {
            String username = principal.getName();
            Customer customer = customerRepository.findByAccount_Username(username)
                    .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                            "Không tìm thấy thông tin khách hàng cho tài khoản: " + username));

            BookingDetailResponseDTO detail = bookingService.getBookingDetail(bookingId, customer.getId());
            return ResponseEntity.ok(detail);
        } catch (BusinessException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi"));
        }
    }

    @PostMapping("/{bookingId}/apply-coupon")
    public ResponseEntity<?> applyCoupon(Principal principal, @PathVariable Long bookingId,
            @RequestBody Map<String, String> payload) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Quý khách cần đăng nhập!"));
        }
        try {
            String username = principal.getName();
            Customer customer = customerRepository.findByAccount_Username(username)
                    .orElseThrow(
                            () -> new BusinessException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng"));

            String couponCode = payload.get("couponCode");
            BigDecimal discountAmount = bookingService.applyCoupon(bookingId, couponCode, customer.getId());

            return ResponseEntity.ok(Map.of("status", "success", "discountAmount", discountAmount));
        } catch (BusinessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(
                    Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi"));
        }
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(
            Principal principal,
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> payload,
            jakarta.servlet.http.HttpServletRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Quý khách cần đăng nhập!"));
        }
        try {
            String username = principal.getName();
            Customer customer = customerRepository.findByAccount_Username(username)
                    .orElseThrow(
                            () -> new BusinessException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng"));

            String fullName = (String) payload.get("fullName");
            String phone = (String) payload.get("phone");
            String email = (String) payload.get("email");
            String cccd = (String) payload.get("cccd");
            String notes = (String) payload.get("notes");

            String paymentMethod = (String) payload.get("paymentMethod");

            bookingService.confirmBooking(bookingId, customer.getId(), fullName, phone, email, cccd, notes);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("message", "Xác nhận đặt phòng thành công!");

            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                String paymentUrl = vnPayService.createPaymentUrl(bookingId, request.getRemoteAddr());
                response.put("paymentUrl", paymentUrl);
            }

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(
                    Map.of("status", "error", "message", e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi"));
        }
    }

}