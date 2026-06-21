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

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:noreply@kawai-resort.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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
        String subject = "Xác nhận đăng ký tài khoản - HOANIEN Retreat Resort";
        String content = buildRegistrationOtpEmail(fullName, otpCode);
        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink, String fullName) {
        String subject = "Đặt lại mật khẩu - HOANIEN Retreat Resort";
        String content = buildPasswordResetEmail(fullName, resetLink);
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

    private String buildRegistrationOtpEmail(String fullName, String otpCode) {
        return "<!DOCTYPE html>" +
                "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                +
                "<div style='background: linear-gradient(135deg, #8B5E3C 0%, #6d4c31 100%); padding: 30px; border-radius: 10px; text-align: center;'>"
                +
                "<h1 style='color: white; margin: 0; font-size: 28px;'>HOANIEN</h1>" +
                "<p style='color: #f5f0e8; margin-top: 5px;'>Retreat Resort & Hub</p>" +
                "</div>" +
                "<div style='background: #f9f6f1; padding: 30px; border-radius: 10px; margin-top: 20px;'>" +
                "<h2 style='color: #2c2416;'>Xác nhận đăng ký tài khoản</h2>" +
                "<p style='color: #5c4a32;'>Kính gửi <strong>" + fullName + "</strong>,</p>" +
                "<p style='color: #5c4a32;'>Cảm ơn bạn đã đăng ký tài khoản tại HOANIEN. Vui lòng sử dụng mã OTP dưới đây để xác nhận:</p>"
                +
                "<div style='background: white; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;'>"
                +
                "<h1 style='color: #8B5E3C; font-size: 36px; letter-spacing: 8px; margin: 0;'>" + otpCode + "</h1>" +
                "</div>" +
                "<p style='color: #8B7355; font-size: 14px;'>Mã OTP có hiệu lực trong 10 phút. Vui lòng không chia sẻ mã này với người khác.</p>"
                +
                "<p style='color: #8B7355; font-size: 14px;'>Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.</p>"
                +
                "</div>" +
                "<div style='text-align: center; margin-top: 20px; color: #8B7355; font-size: 12px;'>" +
                "© 2026 HOANIEN Retreat Resort & Hub" +
                "</div>" +
                "</body></html>";
    }

    private String buildPasswordResetEmail(String fullName, String resetLink) {
        return "<!DOCTYPE html>" +
                "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                +
                "<div style='background: linear-gradient(135deg, #8B5E3C 0%, #6d4c31 100%); padding: 30px; border-radius: 10px; text-align: center;'>"
                +
                "<h1 style='color: white; margin: 0; font-size: 28px;'>HOANIEN</h1>" +
                "<p style='color: #f5f0e8; margin-top: 5px;'>Retreat Resort & Hub</p>" +
                "</div>" +
                "<div style='background: #f9f6f1; padding: 30px; border-radius: 10px; margin-top: 20px;'>" +
                "<h2 style='color: #2c2416;'>Đặt lại mật khẩu</h2>" +
                "<p style='color: #5c4a32;'>Kính gửi <strong>" + fullName + "</strong>,</p>" +
                "<p style='color: #5c4a32;'>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Click vào link bên dưới để tiếp tục:</p>"
                +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetLink
                + "' style='background: #8B5E3C; color: white; padding: 14px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;'>Đặt lại mật khẩu</a>"
                +
                "</div>" +
                "<p style='color: #8B7355; font-size: 14px;'>Link này có hiệu lực trong 15 phút. Vui lòng không chia sẻ link này với người khác.</p>"
                +
                "<p style='color: #8B7355; font-size: 14px;'>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                +
                "</div>" +
                "<div style='text-align: center; margin-top: 20px; color: #8B7355; font-size: 12px;'>" +
                "© 2026 HOANIEN Retreat Resort & Hub" +
                "</div>" +
                "</body></html>";
    }
}
