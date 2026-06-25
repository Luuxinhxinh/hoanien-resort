package com.kawai.services.impl;

import com.kawai.models.Customer;
import com.kawai.models.TourBooking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service gửi email tự động (bất đồng bộ) cho các sự kiện Tour:
 * <ul>
 * <li>Đặt tour thành công → email xác nhận</li>
 * <li>Hủy tour → email thông báo + số tiền hoàn</li>
 * </ul>
 *
 * <p>
 * Email được gửi qua SMTP Gmail, render template HTML bằng Thymeleaf.
 * Toàn bộ method đều {@code @Async} để không block luồng chính.
 */
@Service
public class TourEmailService {

    private static final Logger LOG = LoggerFactory.getLogger(TourEmailService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat VND_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:kawai.resort.noreply@gmail.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Hòa Niên Retreat & Resort}")
    private String fromName;

    @Value("${app.mail.resort-phone:1900 xxxx}")
    private String resortPhone;

    @Value("${app.mail.resort-website:https://hoaniensorretreat.vn}")
    private String resortWebsite;

    public TourEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Gửi email xác nhận đặt tour thành công.
     * Gửi bất đồng bộ (@Async) — không block response HTTP.
     */
    @Async
    public void sendBookingConfirmation(TourBooking booking, Customer customer) {
        sendBookingConfirmation(booking, customer, false, null);
    }

    /**
     * Gửi email xác nhận đặt tour thành công với thông tin ghi nợ vào phòng.
     * Gửi bất đồng bộ (@Async) — không block response HTTP.
     */
    @Async
    public void sendBookingConfirmation(TourBooking booking, Customer customer, boolean postToRoom, String roomDetail) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            LOG.warn("Bỏ qua gửi email xác nhận: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }

        try {
            String tourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName()
                    : "Tour";

            String departureDate = booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null
                    ? booking.getSchedule().getDepartureDate().format(DATE_FMT)
                    : LocalDate.now().format(DATE_FMT);

            String departureTime = booking.getSchedule() != null && booking.getSchedule().getDepartureTime() != null
                    ? booking.getSchedule().getDepartureTime().toString().substring(0, 5)
                    : "--:--";

            // Build Thymeleaf context
            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("tourName", tourName);
            ctx.setVariable("departureDate", departureDate);
            ctx.setVariable("departureTime", departureTime);
            ctx.setVariable("participantCount", booking.getParticipantCount());
            ctx.setVariable("totalPrice", formatVnd(booking.getTotalPrice()));
            ctx.setVariable("bookingDate", LocalDate.now().format(DATE_FMT));
            ctx.setVariable("postToRoom", postToRoom);
            ctx.setVariable("roomDetail", roomDetail);
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);
            ctx.setVariable("bgUrl", "https://i.ibb.co/99JSj0SF/BREmail.png");

            String html = templateEngine.process("email/tour-booking-confirmation", ctx);

            sendHtmlEmail(
                    customer.getEmail(),
                    "Xác nhận đặt tour - " + tourName + " | Hòa Niên Retreat & Resort",
                    html);

            LOG.info("Gửi email xác nhận đặt tour thành công → {} (booking #{})",
                    customer.getEmail(), booking.getId());

        } catch (Exception e) {
            LOG.error("Lỗi khi gửi email xác nhận đặt tour cho booking #{}: {}",
                    booking.getId(), e.getMessage(), e);
            // Không throw — email lỗi không được cancel booking
        }
    }

    /**
     * Gửi email thông báo hủy tour kèm số tiền hoàn.
     * Gửi bất đồng bộ (@Async).
     */
    @Async
    public void sendCancellationNotice(TourBooking booking, Customer customer,
            BigDecimal refundAmount, boolean cancelledByResort) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            LOG.warn("Bỏ qua gửi email hủy tour: customer {} không có email",
                    customer != null ? customer.getId() : "null");
            return;
        }

        try {
            String tourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName()
                    : "Tour";

            String departureDate = booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null
                    ? booking.getSchedule().getDepartureDate().format(DATE_FMT)
                    : "--/--/----";

            BigDecimal forfeitAmount = booking.getTotalPrice() != null && refundAmount != null
                    ? booking.getTotalPrice().subtract(refundAmount)
                    : BigDecimal.ZERO;

            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("tourName", tourName);
            ctx.setVariable("departureDate", departureDate);
            ctx.setVariable("participantCount", booking.getParticipantCount());
            ctx.setVariable("totalPrice", formatVnd(booking.getTotalPrice()));
            ctx.setVariable("refundAmount", formatVnd(refundAmount));
            ctx.setVariable("forfeitAmount", formatVnd(forfeitAmount));
            ctx.setVariable("cancelledByResort", cancelledByResort);
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);
            ctx.setVariable("bgUrl", "https://i.ibb.co/99JSj0SF/BREmail.png");

            String html = templateEngine.process("email/tour-booking-cancelled", ctx);

            String subject = cancelledByResort
                    ? "❌ Thông báo hủy tour — " + tourName + " | Hòa Niên Retreat & Resort"
                    : "❌ Xác nhận hủy tour — " + tourName + " | Hòa Niên Retreat & Resort";

            sendHtmlEmail(customer.getEmail(), subject, html);

            LOG.info("Gửi email hủy tour thành công → {} (booking #{}, hoàn {})",
                    customer.getEmail(), booking.getId(), formatVnd(refundAmount));

        } catch (Exception e) {
            LOG.error("Lỗi khi gửi email hủy tour cho booking #{}: {}",
                    booking.getId(), e.getMessage(), e);
        }
    }

    /**
     * Gửi email yêu cầu đặt lại mật khẩu.
     * Gửi bất đồng bộ (@Async).
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        if (toEmail == null || toEmail.isBlank()) {
            LOG.warn("Bỏ qua gửi email reset password: email rỗng");
            return;
        }

        try {
            String resetLink = resortWebsite + "/auth/reset-password?token=" + token;

            Context ctx = new Context(new Locale("vi", "VN"));
            ctx.setVariable("username", username);
            ctx.setVariable("resetLink", resetLink);
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);

            String html = templateEngine.process("email/reset-password-mail", ctx);
            sendHtmlEmail(toEmail, "Yêu cầu đặt lại mật khẩu | Hòa Niên Retreat & Resort", html);

            LOG.info("Gửi email reset password thành công → {}", toEmail);
        } catch (Exception e) {
            LOG.error("Lỗi khi gửi email reset password cho {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // ─── Internal helper ──────────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody)
            throws MessagingException, java.io.UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML

        try {
            org.springframework.core.io.ClassPathResource res = new org.springframework.core.io.ClassPathResource(
                    "static/images/email-bg.png");
            if (res.exists()) {
                helper.addInline("emailBg", res);
            }
        } catch (Exception e) {
            LOG.error("Failed to add inline image to email: {}", e.getMessage(), e);
        }

        mailSender.send(message);
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null)
            return "0 ₫";
        return VND_FMT.format(amount) + " ₫";
    }
}
