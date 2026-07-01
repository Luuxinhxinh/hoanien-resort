package com.kawai.controllers.api;

import com.kawai.dto.BookingRequestDTO;
import com.kawai.dto.BookingResponseDTO;
import com.kawai.dto.BookingApiResponse;
import com.kawai.dto.BookingDetailResponseDTO;
import com.kawai.models.Customer;
import com.kawai.repositories.CustomerRepository;
import com.kawai.services.interfaces.BookingService;
import com.kawai.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private String extractUsername(Principal principal) {
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.core.user.OAuth2User oauthUser = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal)
                    .getPrincipal();
            String email = oauthUser.getAttribute("email");
            if (email != null) {
                return email;
            }
        }
        return principal.getName();
    }

    private Customer resolveCurrentCustomer(Principal principal) {
        String username = extractUsername(principal);
        Customer customer = customerRepository.findByAccount_Username(username)
                .orElseGet(() -> customerRepository.findByEmail(username).orElse(null));
        if (customer == null) {
            throw new BusinessException("CUSTOMER_NOT_FOUND",
                    "Không tìm thấy thông tin khách hàng cho tài khoản: " + username);
        }
        return customer;
    }

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private com.kawai.repositories.RoomGuestRepository roomGuestRepository;

    @Autowired
    private com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private com.kawai.repositories.DependentRepository dependentRepository;

    @Autowired
    private com.kawai.repositories.PromotionRepository promotionRepository;

    @Autowired
    private com.kawai.repositories.BookingRepository bookingRepository;

    @Autowired
    private com.kawai.services.interfaces.VnPayService vnPayService;

    @PostMapping
    public ResponseEntity<?> createBooking(Principal principal, @RequestBody BookingRequestDTO request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new BookingApiResponse(
                    "error", null, null, null, null, "Quý khách cần đăng nhập để thực hiện đặt phòng!"));
        }

        try {
            Customer customer = resolveCurrentCustomer(principal);

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
            // Trả về tổng chính thức từ backend (bao gồm phụ thu + khuyến mãi)
            apiResponse.setDiscountedPrice(response.getDiscountedPrice());

            return ResponseEntity.ok(apiResponse);
        } catch (BusinessException e) {
            return ResponseEntity.status(400).body(new BookingApiResponse(
                    "error", null, null, null, null, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(new BookingApiResponse(
                    "error", null, null, null, null,
                    e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định"));
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(Principal principal, @PathVariable Long bookingId, @RequestBody(required = false) com.kawai.dto.CancelRequestDTO cancelRequest) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new BookingApiResponse(
                    "error", null, null, null, null, "Quý khách cần đăng nhập để thực hiện thao tác này!"));
        }

        try {
            Customer customer = resolveCurrentCustomer(principal);

            BookingResponseDTO response = bookingService.cancelBooking(bookingId, customer.getId(), cancelRequest);

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
                    "error", null, null, null, null, e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.status(400).body(new BookingApiResponse(
                    "error", null, null, null, null, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new BookingApiResponse(
                    "error", null, null, null, null,
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
            Customer customer = resolveCurrentCustomer(principal);

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
            Customer customer = resolveCurrentCustomer(principal);

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
            Customer customer = resolveCurrentCustomer(principal);

            String fullName = (String) payload.get("fullName");
            String phone = (String) payload.get("phone");
            String email = (String) payload.get("email");
            String cccd = (String) payload.get("cccd");
            String notes = (String) payload.get("notes");

            String paymentMethod = (String) payload.get("paymentMethod");

            // 2. Chốt booking: Xác nhận available, gắn thông tin khách, chuyển sang
            // Pending_Payment hoặc Confirmed
            bookingService.confirmBooking(bookingId, customer.getId(), fullName, phone, email, cccd, null, notes,
                    paymentMethod);

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

    @GetMapping("/{bookingId}/folios")
    public ResponseEntity<?> getBookingFolios(Principal principal, @PathVariable Long bookingId) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Quý khách cần đăng nhập!"));
        }
        try {
            Customer customer = resolveCurrentCustomer(principal);
            java.util.List<Map<String, Object>> folios = bookingService.getBookingFolios(bookingId, customer.getId());
            return ResponseEntity.ok(folios);
        } catch (BusinessException e) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("status", "error", "message",
                            e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi hệ thống"));
        }
    }

    @GetMapping("/{bookingId}/guests")
    public ResponseEntity<?> getBookingGuests(Principal principal, @PathVariable Long bookingId) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Quý khách cần đăng nhập!"));
        }
        try {
            Customer customer = resolveCurrentCustomer(principal);

            // Lấy danh sách RoomBookingDetail của đơn này
            java.util.List<com.kawai.models.RoomBookingDetail> details = roomBookingDetailRepository
                    .findByRoomBookingId(bookingId);

            // Trả về cấu trúc: [ { detailId, roomName, guests: [ { guestId, type,
            // dependentId, isPrimary } ] } ]
            java.util.List<Map<String, Object>> responseList = new java.util.ArrayList<>();

            for (com.kawai.models.RoomBookingDetail detail : details) {
                // Kiểm tra xem đơn này có thuộc về customer không (Chủ đơn)
                if (!detail.getRoomBooking().getCustomer().getId().equals(customer.getId())) {
                    throw new BusinessException("FORBIDDEN", "Không có quyền truy cập đơn hàng này");
                }

                Map<String, Object> roomData = new java.util.HashMap<>();
                roomData.put("detailId", detail.getId());
                roomData.put("roomName",
                        detail.getCategory() != null ? detail.getCategory().getCategoryName() : "Phòng");

                java.util.List<com.kawai.models.RoomGuest> guests = roomGuestRepository
                        .findByRoomBookingDetailId(detail.getId());
                java.util.List<Map<String, Object>> guestsData = new java.util.ArrayList<>();
                for (com.kawai.models.RoomGuest g : guests) {
                    Map<String, Object> gData = new java.util.HashMap<>();
                    gData.put("guestId", g.getId());
                    gData.put("type", g.getGuestType());
                    gData.put("dependentId", g.getDependent() != null ? g.getDependent().getId() : null);
                    gData.put("isPrimary", g.getIsPrimaryContact());
                    gData.put("name", g.getDependent() != null ? g.getDependent().getDependentName()
                            : (g.getCustomer() != null ? g.getCustomer().getFullName() : ""));
                    guestsData.add(gData);
                }

                roomData.put("guests", guestsData);
                responseList.add(roomData);
            }
            return ResponseEntity.ok(responseList);

        } catch (BusinessException e) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("status", "error", "message",
                            e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi hệ thống"));
        }
    }

    @PostMapping("/{bookingId}/guests")
    public ResponseEntity<?> updateBookingGuests(Principal principal, @PathVariable Long bookingId,
            @RequestBody java.util.List<Map<String, Object>> updates) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Quý khách cần đăng nhập!"));
        }
        try {
            Customer customer = resolveCurrentCustomer(principal);

            for (Map<String, Object> update : updates) {
                Long guestId = Long.valueOf(update.get("guestId").toString());
                Long dependentId = update.get("dependentId") != null && !update.get("dependentId").toString().isEmpty()
                        ? Long.valueOf(update.get("dependentId").toString())
                        : null;

                com.kawai.models.RoomGuest guest = roomGuestRepository.findById(guestId).orElse(null);
                if (guest != null) {
                    // Check ownership
                    if (guest.getRoomBookingDetail().getRoomBooking().getCustomer().getId().equals(customer.getId())) {
                        if (dependentId != null) {
                            com.kawai.models.Dependent dep = dependentRepository.findById(dependentId).orElse(null);
                            if (dep != null && dep.getCustomer().getId().equals(customer.getId())) {
                                guest.setDependent(dep);
                            }
                        } else {
                            // If primary contact, keep customer, otherwise null dependent
                            if (!Boolean.TRUE.equals(guest.getIsPrimaryContact())) {
                                guest.setDependent(null);
                            }
                        }
                        roomGuestRepository.save(guest);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "Khai báo khách lưu trú thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("status", "error", "message",
                            e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi hệ thống"));
        }
    }

    @PostMapping("/{bookingId}/rooms/{detailId}/credit-limit")
    public ResponseEntity<?> updateRoomCreditLimit(Principal principal, @PathVariable Long bookingId,
            @PathVariable Long detailId, @RequestBody Map<String, Object> payload) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Quý khách cần đăng nhập!"));
        }
        try {
            Customer customer = resolveCurrentCustomer(principal);
            com.kawai.models.RoomBookingDetail detail = roomBookingDetailRepository.findById(detailId).orElse(null);
            if (detail == null || !detail.getRoomBooking().getId().equals(bookingId)) {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Không tìm thấy phòng!"));
            }

            com.kawai.models.RoomBooking booking = detail.getRoomBooking();
            if (!booking.getCustomer().getId().equals(customer.getId())) {
                return ResponseEntity.status(403)
                        .body(Map.of("status", "error", "message", "Không có quyền truy cập!"));
            }

            if (payload.get("newLimit") == null || payload.get("newLimit").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Hạn mức không hợp lệ!"));
            }
            BigDecimal newLimit;
            try {
                newLimit = new BigDecimal(payload.get("newLimit").toString());
            } catch (Exception ex) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "Định dạng số không hợp lệ!"));
            }

            if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Hạn mức không hợp lệ!"));
            }

            BigDecimal maxLimit = booking.getCreditLimit() != null ? booking.getCreditLimit() : BigDecimal.ZERO;
            BigDecimal totalOtherRoomsLimit = BigDecimal.ZERO;
            java.util.List<com.kawai.models.RoomBookingDetail> allDetails = roomBookingDetailRepository
                    .findByRoomBookingId(bookingId);
            for (com.kawai.models.RoomBookingDetail d : allDetails) {
                if (!d.getId().equals(detailId)) {
                    totalOtherRoomsLimit = totalOtherRoomsLimit
                            .add(d.getSubCreditLimit() != null ? d.getSubCreditLimit() : BigDecimal.ZERO);
                }
            }

            if (totalOtherRoomsLimit.add(newLimit).compareTo(maxLimit) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message",
                                "Tổng hạn mức cấp cho các phòng không được vượt quá hạn mức tối đa của đơn đặt phòng ("
                                        + String.format("%,.0f", maxLimit) + "đ)"));
            }

            detail.setSubCreditLimit(newLimit);
            roomBookingDetailRepository.save(detail);

            return ResponseEntity.ok(Map.of("status", "success", "message", "Cập nhật hạn mức phòng thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Đã xảy ra lỗi hệ thống"));
        }
    }

    /**
     * Validate và tính toán giá trị mã giảm giá từ bảng Promotions.
     * Endpoint này được đặt dưới /api/bookings/** nên được truy cập bởi guest (không cần đăng nhập).
     *
     * @param code   Mã giảm giá cần kiểm tra
     * @param amount Số tiền gốc (VND) để tính toán giá trị giảm
     */
    @org.springframework.web.bind.annotation.GetMapping("/promo/validate")
    public ResponseEntity<?> validatePromo(
            @org.springframework.web.bind.annotation.RequestParam String code,
            @org.springframework.web.bind.annotation.RequestParam java.math.BigDecimal amount,
            java.security.Principal principal) {
        try {
            java.util.Optional<com.kawai.models.Promotion> optPromo = promotionRepository.findByPromoCode(code.toUpperCase().trim());
            if (optPromo.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Mã giảm giá không tồn tại"));
            }
            if (principal != null) {
                Customer customer = resolveCurrentCustomer(principal);
                if (customer != null) {
                    java.util.List<com.kawai.models.Booking> usedBookings = bookingRepository.findUsedPromoBookings(customer.getId(), code.toUpperCase().trim());
                    if (!usedBookings.isEmpty()) {
                        com.kawai.models.Booking b = usedBookings.get(0);
                        String serviceName = "Dịch vụ của resort";
                        if (b instanceof com.kawai.models.TourBooking) {
                            com.kawai.models.TourBooking tb = (com.kawai.models.TourBooking) b;
                            if (tb.getSchedule() != null && tb.getSchedule().getTour() != null) {
                                serviceName = "Tour " + tb.getSchedule().getTour().getTourName();
                            } else {
                                serviceName = "Đặt Tour du lịch";
                            }
                        } else if (b instanceof com.kawai.models.RoomBooking) {
                            serviceName = "Đặt phòng nghỉ";
                        }
                        return ResponseEntity.ok(Map.of("success", false, "message",
                            "Mã giảm giá \"" + code.toUpperCase().trim() + "\" đã được sử dụng tại dịch vụ \"" + serviceName + "\". Hãy nhập mã giảm giá mới."));
                    }
                }
            }
            com.kawai.models.Promotion promo = optPromo.get();
            if (!Boolean.TRUE.equals(promo.getIsActive())
                    || (promo.getValidTo() != null && promo.getValidTo().isBefore(java.time.LocalDate.now()))) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Mã giảm giá đã hết hạn hoặc không hoạt động"));
            }
            java.math.BigDecimal discountRate = promo.getDiscountValue();
            java.math.BigDecimal discountAmount;
            // Heuristic: giá trị >= 100 được coi là FIXED_AMOUNT (VND), còn lại là PERCENTAGE
            boolean isFixed = "FIXED_AMOUNT".equalsIgnoreCase(promo.getDiscountType())
                    || discountRate.compareTo(new java.math.BigDecimal("100")) >= 0;
            if (isFixed) {
                discountAmount = discountRate;
            } else {
                discountAmount = amount.multiply(discountRate)
                        .divide(new java.math.BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
            }
            if (discountAmount.compareTo(amount) > 0) discountAmount = amount;
            java.math.BigDecimal newAmount = amount.subtract(discountAmount);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "promoCode", promo.getPromoCode(),
                    "discountType", isFixed ? "FIXED_AMOUNT" : "PERCENTAGE",
                    "discountValue", discountRate,
                    "discountAmount", discountAmount,
                    "newAmount", newAmount,
                    "description", promo.getDescription() != null ? promo.getDescription() : ""));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Lỗi kiểm tra mã giảm giá: " + e.getMessage()));
        }
    }

}