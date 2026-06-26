package com.kawai.services.impl;

import com.kawai.models.ConsolidatedInvoice;
import com.kawai.services.interfaces.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final java.time.format.DateTimeFormatter DATE_FMT = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final java.text.NumberFormat VND_FMT = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));

    @org.springframework.beans.factory.annotation.Autowired
    private org.thymeleaf.TemplateEngine templateEngine;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:hoanien.00@gmail.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail.resort-phone:1900 xxxx}")
    private String resortPhone;

    @Value("${app.mail.resort-website:https://hoaniensorretreat.vn}")
    private String resortWebsite;

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendBookingConfirmation(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer) {
        sendBookingConfirmation(booking, customer, false, null);
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendBookingConfirmation(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer, boolean postToRoom, String roomDetail) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email xác nhận: customer {} không có email", customer != null ? customer.getId() : "null");
            return;
        }

        try {
            String tourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName() : "Tour";
            String departureDate = booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null
                    ? booking.getSchedule().getDepartureDate().format(DATE_FMT) : java.time.LocalDate.now().format(DATE_FMT);
            String departureTime = booking.getSchedule() != null && booking.getSchedule().getDepartureTime() != null
                    ? booking.getSchedule().getDepartureTime().toString().substring(0, 5) : "--:--";

            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
            ctx.setVariable("customerName", customer.getFullName());
            ctx.setVariable("bookingId", booking.getId());
            ctx.setVariable("tourName", tourName);
            ctx.setVariable("departureDate", departureDate);
            ctx.setVariable("departureTime", departureTime);
            ctx.setVariable("participantCount", booking.getParticipantCount());
            ctx.setVariable("totalPrice", formatVnd(booking.getTotalPrice()));
            ctx.setVariable("bookingDate", java.time.LocalDate.now().format(DATE_FMT));
            ctx.setVariable("postToRoom", postToRoom);
            ctx.setVariable("roomDetail", roomDetail);
            ctx.setVariable("resortPhone", resortPhone);
            ctx.setVariable("resortWebsite", resortWebsite);
            ctx.setVariable("bgUrl", "https://i.ibb.co/99JSj0SF/BREmail.png");

            String html = templateEngine.process("email/tour-booking-confirmation", ctx);
            sendEmail(customer.getEmail(), "Xác nhận đặt tour - " + tourName + " | Hòa Niên Retreat & Resort", html);
            logger.info("Gửi email xác nhận đặt tour thành công → {} (booking #{})", customer.getEmail(), booking.getId());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email xác nhận đặt tour cho booking #{}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    @org.springframework.scheduling.annotation.Async
    @Override
    public void sendCancellationNotice(com.kawai.models.TourBooking booking, com.kawai.models.Customer customer, java.math.BigDecimal refundAmount, boolean cancelledByResort) {
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            logger.warn("Bỏ qua gửi email hủy tour: customer {} không có email", customer != null ? customer.getId() : "null");
            return;
        }

        try {
            String tourName = booking.getSchedule() != null && booking.getSchedule().getTour() != null
                    ? booking.getSchedule().getTour().getTourName() : "Tour";
            String departureDate = booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null
                    ? booking.getSchedule().getDepartureDate().format(DATE_FMT) : "--/--/----";

            java.math.BigDecimal forfeitAmount = booking.getTotalPrice() != null && refundAmount != null
                    ? booking.getTotalPrice().subtract(refundAmount) : java.math.BigDecimal.ZERO;

            org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
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
            String subject = cancelledByResort ? "❌ Thông báo hủy tour — " + tourName + " | Hòa Niên Retreat & Resort"
                    : "❌ Xác nhận hủy tour — " + tourName + " | Hòa Niên Retreat & Resort";

            sendEmail(customer.getEmail(), subject, html);
            logger.info("Gửi email hủy tour thành công → {} (booking #{}, hoàn {})", customer.getEmail(), booking.getId(), formatVnd(refundAmount));
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email hủy tour cho booking #{}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    private String formatVnd(java.math.BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return VND_FMT.format(amount) + " ₫";
    }

    @Override
    public void sendInvoiceEmail(String toEmail, ConsolidatedInvoice invoice, String pdfAttachmentPath) {
        if (!"Paid".equalsIgnoreCase(invoice.getInvoiceStatus())) {
            throw new IllegalStateException(
                    "Không thể gửi Email cho hóa đơn chưa được thanh toán (Trạng thái hiện tại: "
                            + invoice.getInvoiceStatus() + ")");
        }

        // Mô phỏng việc kết nối SMTP và gửi email
        logger.info("================================================");
        logger.info("[EMAIL SERVICE] KẾT NỐI SMTP THÀNH CÔNG");
        logger.info("[EMAIL SERVICE] Đang gửi thư tới: {}", toEmail);
        logger.info("[EMAIL SERVICE] Chủ đề: Hóa đơn điện tử e-Invoice số {}", invoice.getInvoiceNumber());
        logger.info("[EMAIL SERVICE] Nội dung: Kính gửi quý khách, đính kèm là hóa đơn thanh toán tiền phòng/dịch vụ.");
        logger.info("[EMAIL SERVICE] File đính kèm: {}", pdfAttachmentPath);
        logger.info("[EMAIL SERVICE] TRẠNG THÁI: ĐÃ GỬI THÀNH CÔNG");
        logger.info("================================================");
    }

    @Override
    public void sendRegistrationOtpEmail(String toEmail, String otpCode, String fullName) {
        String subject = "Xác nhận đăng ký tài khoản - HOANIEN Retreat & Resort";
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
        ctx.setVariable("customerName", fullName);
        ctx.setVariable("otpCode", otpCode);
        ctx.setVariable("resortPhone", resortPhone);
        ctx.setVariable("resortWebsite", resortWebsite);
        String content = templateEngine.process("email/registration-otp", ctx);
        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink, String fullName) {
        String subject = "Đặt lại mật khẩu - HOANIEN Retreat & Resort";
        org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context(new java.util.Locale("vi", "VN"));
        ctx.setVariable("customerName", fullName);
        ctx.setVariable("otpCode", resetLink);
        ctx.setVariable("resortPhone", resortPhone);
        ctx.setVariable("resortWebsite", resortWebsite);
        String content = templateEngine.process("email/password-reset", ctx);
        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            logger.warn("[SENDGRID] API_KEY chưa được cấu hình. Email không được gửi.");
            logger.info("[SENDGRID MOCK] To: {}, Subject: {}", toEmail, subject);
            return;
        }

        try {
            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            logger.info("[SENDGRID] Email sent to {} - Status: {}", toEmail, response.getStatusCode());
        } catch (IOException e) {
            logger.error("[SENDGRID] Lỗi gửi email tới {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public void sendCustomWorkflowEmail(String customFromEmail, String toEmail, String subject, String htmlContent) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            logger.warn("[SENDGRID] API_KEY chưa được cấu hình. Custom Workflow Email không được gửi.");
            logger.info("[SENDGRID MOCK] From: {}, To: {}, Subject: {}", customFromEmail, toEmail, subject);
            logger.info("[SENDGRID MOCK CONTENT]: \n{}", htmlContent);
            return;
        }

        try {
            String finalFrom = (customFromEmail != null && !customFromEmail.trim().isEmpty()) ? customFromEmail : fromEmail;
            Email from = new Email(finalFrom);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            logger.info("[SENDGRID] Custom Workflow Email sent from {} to {} - Status: {}", finalFrom, toEmail, response.getStatusCode());
        } catch (IOException e) {
            logger.error("[SENDGRID] Lỗi gửi custom workflow email tới {}: {}", toEmail, e.getMessage());
        }
    }



    @Override
    public void sendSlaWarningEmail(String toEmail, String taskName, int pendingMinutes, String roomNumber) {
        String subject = "[SLA Warning] Nhiệm vụ chưa nhận việc quá hạn - HOANIEN Resort";
        String htmlContent = "<!DOCTYPE html><html><body style=\"font-family: 'Times New Roman', Times, serif;\">" +
                "<h2 style=\"color: #b73e3e;\">CẢNH BÁO SLA QUÁ HẠN NHẬN VIỆC</h2>" +
                "<p>Kính gửi Supervisor,</p>" +
                "<p>Hệ thống phát hiện nhiệm vụ sau đã quá hạn thời gian nhận việc theo quy định:</p>" +
                "<ul>" +
                "<li><strong>Nhiệm vụ:</strong> " + taskName + "</li>" +
                "<li><strong>Phòng:</strong> " + (roomNumber != null ? roomNumber : "N/A") + "</li>" +
                "<li><strong>Thời gian chờ:</strong> " + pendingMinutes + " phút (Vượt ngưỡng quy định)</li>" +
                "</ul>" +
                "<p>Vui lòng đăng nhập hệ thống Admin để điều phối và xử lý ngay lập tức.</p>" +
                "<br/><p>Trân trọng,<br/><i>HOANIEN Operational Workflow Engine</i></p>" +
                "</body></html>";
        sendEmail(toEmail, subject, htmlContent);
    }
}
