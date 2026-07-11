package com.kawai.controllers;

import com.kawai.services.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * One-shot endpoint to preview all email templates by sending them to a test address.
 * Accessible only by ADMIN role.
 */
@RestController
@RequestMapping("/admin/test-emails")
@PreAuthorize("hasRole('ADMIN')")
public class EmailPreviewController {

    private static final Logger log = LoggerFactory.getLogger(EmailPreviewController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final java.text.NumberFormat VND_FMT = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailService emailService;

    /**
     * POST /admin/test-emails?to=target@email.com
     * Renders and sends all 9 email templates with sample data.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendAll(
            @RequestParam(value = "to", defaultValue = "liungu2005@gmail.com") String to) {

        Map<String, Object> results = new LinkedHashMap<>();
        List<String> sent = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        // 1. registration-otp
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Nguyễn Văn Test");
            ctx.setVariable("otpCode", "847291");
            ctx.setVariable("expiryMinutes", 10);
            ctx.setVariable("resortPhone", "1900 1234");
            ctx.setVariable("resortWebsite", "https://hoanienspa.vn");
            String html = templateEngine.process("email/registration-otp", ctx);
            emailService.sendEmail(to, "[TEST] Xác thực OTP đăng ký tài khoản", html);
            sent.add("registration-otp");
            log.info("Sent registration-otp to {}", to);
        } catch (Exception e) {
            failed.add("registration-otp: " + e.getMessage());
            log.error("Failed registration-otp", e);
        }

        // 2. password-reset
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Nguyễn Văn Test");
            ctx.setVariable("resetLink", "http://localhost:8080/auth/reset-password?token=SAMPLE_TOKEN_12345");
            ctx.setVariable("expiryMinutes", 30);
            ctx.setVariable("resortPhone", "1900 1234");
            ctx.setVariable("resortWebsite", "https://hoanienspa.vn");
            String html = templateEngine.process("email/password-reset", ctx);
            emailService.sendEmail(to, "[TEST] Đặt lại mật khẩu tài khoản HOANIEN", html);
            sent.add("password-reset");
        } catch (Exception e) {
            failed.add("password-reset: " + e.getMessage());
        }

        // 3. booking-table (Table Reservation Confirmation)
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Trần Thị Test");
            ctx.setVariable("reservationId", 1001L);
            ctx.setVariable("reservationDate", LocalDate.now().plusDays(2).format(DATE_FMT));
            ctx.setVariable("startTime", "18:30");
            ctx.setVariable("endTime", "20:30");
            ctx.setVariable("tableNumber", "T05");
            ctx.setVariable("guestCount", 4);
            ctx.setVariable("note", "Yêu cầu bàn view hồ bơi, ít cay");
            String html = templateEngine.process("email/booking-table", ctx);
            emailService.sendEmail(to, "[TEST] Xác nhận đặt bàn thành công #1001", html);
            sent.add("booking-table");
        } catch (Exception e) {
            failed.add("booking-table: " + e.getMessage());
        }

        // 4. extend-hold (Extend Table Hold)
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Trần Thị Test");
            ctx.setVariable("reservationId", 1001L);
            ctx.setVariable("extendMinutes", 15);
            ctx.setVariable("latestCheckInTime", "18:45");
            String html = templateEngine.process("email/extend-hold", ctx);
            emailService.sendEmail(to, "[TEST] Gia hạn giữ bàn thành công #1001", html);
            sent.add("extend-hold");
        } catch (Exception e) {
            failed.add("extend-hold: " + e.getMessage());
        }

        // 5. cancel-booking (Cancel Table Booking)
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Lê Văn Test");
            ctx.setVariable("reservationId", 1002L);
            ctx.setVariable("reservationTime", "19:00");
            ctx.setVariable("reservationDate", LocalDate.now().format(DATE_FMT));
            String html = templateEngine.process("email/cancel-booking", ctx);
            emailService.sendEmail(to, "[TEST] Thông báo hủy đặt bàn #1002", html);
            sent.add("cancel-booking");
        } catch (Exception e) {
            failed.add("cancel-booking: " + e.getMessage());
        }

        // 6. room-service (Room Service Confirmation)
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Phạm Thị Test");
            ctx.setVariable("orderId", 5001L);
            ctx.setVariable("roomNumber", "101");
            ctx.setVariable("totalAmount", VND_FMT.format(new BigDecimal("350000")) + " ₫");
            ctx.setVariable("paymentMethod", "Ghi nợ vào phòng");
            ctx.setVariable("items", "Phở bò x1, Gỏi cuốn x2, Nước ép dứa x2");
            String html = templateEngine.process("email/room-service", ctx);
            emailService.sendEmail(to, "[TEST] Xác nhận đơn phục vụ tại phòng #5001", html);
            sent.add("room-service");
        } catch (Exception e) {
            failed.add("room-service: " + e.getMessage());
        }

        // 7. invoice (Checkout Invoice)
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Nguyễn Văn Test");
            ctx.setVariable("invoiceNumber", "INV-2026-00042");
            ctx.setVariable("issuedDate", LocalDate.now().format(DATE_FMT));
            ctx.setVariable("roomNumber", "101");

            List<Map<String, String>> items = new ArrayList<>();
            Map<String, String> item1 = new LinkedHashMap<>();
            item1.put("date", LocalDate.now().minusDays(3).format(DATE_FMT));
            item1.put("description", "Tiền phòng (Deluxe Ocean View) – 3 đêm");
            item1.put("amount", VND_FMT.format(new BigDecimal("4500000")) + " ₫");
            items.add(item1);
            Map<String, String> item2 = new LinkedHashMap<>();
            item2.put("date", LocalDate.now().minusDays(2).format(DATE_FMT));
            item2.put("description", "Dịch vụ phục vụ tại phòng (Room Service)");
            item2.put("amount", VND_FMT.format(new BigDecimal("350000")) + " ₫");
            items.add(item2);
            ctx.setVariable("items", items);

            BigDecimal subtotal = new BigDecimal("4850000");
            BigDecimal vat = subtotal.multiply(new BigDecimal("0.10"));
            BigDecimal deposit = new BigDecimal("500000");
            BigDecimal total = subtotal.add(vat).subtract(deposit);

            ctx.setVariable("subtotal", VND_FMT.format(subtotal) + " ₫");
            ctx.setVariable("vat", VND_FMT.format(vat) + " ₫");
            ctx.setVariable("depositPaid", VND_FMT.format(deposit) + " ₫");
            ctx.setVariable("totalDue", VND_FMT.format(total) + " ₫");
            ctx.setVariable("paymentMethod", "Chuyển khoản ngân hàng");
            String html = templateEngine.process("email/invoice", ctx);
            emailService.sendEmail(to, "[TEST] Hóa đơn điện tử HOANIEN – INV-2026-00042", html);
            sent.add("invoice");
        } catch (Exception e) {
            failed.add("invoice: " + e.getMessage());
        }

        // 8. tour-booking-confirmation
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Nguyễn Văn Test");
            ctx.setVariable("bookingId", "TOUR-2026-0099");
            ctx.setVariable("tourName", "Khám phá Hội An 1 ngày");
            ctx.setVariable("departureDate", LocalDate.now().plusDays(5).format(DATE_FMT));
            ctx.setVariable("departureTime", "08:00");
            ctx.setVariable("participantCount", 3);
            ctx.setVariable("postToRoom", false);
            ctx.setVariable("roomDetail", "Phòng 201");
            ctx.setVariable("bookingDate", LocalDate.now().format(DATE_FMT));
            ctx.setVariable("totalPrice", VND_FMT.format(new BigDecimal("2700000")) + " ₫");
            ctx.setVariable("resortPhone", "1900 1234");
            ctx.setVariable("resortWebsite", "https://hoanienspa.vn");
            
            // Mock insurance variables for email preview
            ctx.setVariable("hasInsurance", true);
            ctx.setVariable("insurancePolicyNumber", "INS-20260711-SCH5-A3E9");
            java.util.List<java.util.Map<String, String>> mockAttendees = new java.util.ArrayList<>();
            java.util.Map<String, String> att1 = new java.util.HashMap<>();
            att1.put("name", "NGUYỄN VĂN A");
            att1.put("cccd", "0123456789xx");
            mockAttendees.add(att1);
            java.util.Map<String, String> att2 = new java.util.HashMap<>();
            att2.put("name", "TRẦN THỊ B");
            att2.put("cccd", "0987654321xx");
            mockAttendees.add(att2);
            ctx.setVariable("formattedAttendees", mockAttendees);
            
            String html = templateEngine.process("email/tour-booking-confirmation", ctx);
            emailService.sendEmail(to, "[TEST] Xác nhận đặt tour – TOUR-2026-0099", html);
            sent.add("tour-booking-confirmation");
        } catch (Exception e) {
            failed.add("tour-booking-confirmation: " + e.getMessage());
        }

        // 9. tour-booking-cancelled
        try {
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", "Nguyễn Văn Test");
            ctx.setVariable("bookingRef", "TOUR-2026-0099");
            ctx.setVariable("tourName", "Khám phá Hội An 1 ngày");
            ctx.setVariable("tourDate", LocalDate.now().plusDays(5).format(DATE_FMT));
            ctx.setVariable("refundAmount", VND_FMT.format(new BigDecimal("900000")) + " ₫");
            ctx.setVariable("cancelledByResort", false);
            ctx.setVariable("resortPhone", "1900 1234");
            ctx.setVariable("resortWebsite", "https://hoanienspa.vn");
            String html = templateEngine.process("email/tour-booking-cancelled", ctx);
            emailService.sendEmail(to, "[TEST] Hủy tour – TOUR-2026-0099", html);
            sent.add("tour-booking-cancelled");
        } catch (Exception e) {
            failed.add("tour-booking-cancelled: " + e.getMessage());
        }

        results.put("sentTo", to);
        results.put("total", sent.size() + failed.size());
        results.put("sent", sent);
        results.put("failed", failed);
        return ResponseEntity.ok(results);
    }
}
