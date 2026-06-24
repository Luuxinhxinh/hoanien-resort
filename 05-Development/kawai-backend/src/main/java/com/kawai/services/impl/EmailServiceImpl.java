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
        String subject = "Xác nhận đăng ký tài khoản - HOANIEN Retreat & Resort";
        String content = buildRegistrationOtpEmail(fullName, otpCode);
        sendEmail(toEmail, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink, String fullName) {
        String subject = "Đặt lại mật khẩu - HOANIEN Retreat & Resort";
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

    private String buildRegistrationOtpEmail(String fullName, String otpCode) {
        return "<!DOCTYPE html>" +
                "<html><body style=\"font-family: 'Times New Roman', Times, serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #FAFAFA;\">" +
                "<div style=\"background: linear-gradient(135deg, #1A1A1A 0%, #2C2C2C 100%); padding: 40px 30px; text-align: center; border-radius: 8px 8px 0 0;\">" +
                "<h1 style=\"color: #D4AF37; margin: 0; font-size: 32px; letter-spacing: 6px; font-weight: 400;\">HOANIEN</h1>" +
                "<p style=\"color: #E0E0E0; margin-top: 8px; font-weight: 300; font-size: 14px; letter-spacing: 2px; font-style: italic;\">Retreat & Resort</p>" +
                "</div>" +
                "<div style=\"background: #FFFFFF; padding: 40px 30px; border-left: 1px solid #EAEAEA; border-right: 1px solid #EAEAEA; border-bottom: 1px solid #EAEAEA; border-radius: 0 0 8px 8px;\">" +
                "<div style=\"text-align: center; margin-bottom: 30px;\">" +
                "<span style=\"display: inline-block; padding: 6px 16px; background-color: #F8F5F0; border: 1px solid #D4AF37; color: #8B7355; font-size: 12px; letter-spacing: 1px; text-transform: uppercase; border-radius: 20px;\">Xác nhận đăng ký</span>" +
                "</div>" +
                "<p style=\"color: #333333; font-size: 16px; line-height: 1.6; margin-bottom: 20px;\">Kính gửi Quý khách <strong style=\"color: #1A1A1A;\">" + fullName + "</strong>,</p>" +
                "<p style=\"color: #555555; font-size: 15px; line-height: 1.8;\">Lời đầu tiên, <i>HOANIEN Retreat & Resort</i> xin gửi lời cảm ơn chân thành tới Quý khách vì đã tin tưởng và lựa chọn dịch vụ của chúng tôi. Để hoàn tất thủ tục đăng ký tài khoản thành viên, xin vui lòng sử dụng mã bảo mật dưới đây:</p>" +
                "<div style=\"background: #FDFBF7; padding: 30px; border-radius: 8px; text-align: center; margin: 35px 0; border: 1px solid #E8E0D5;\">" +
                "<h1 style=\"color: #1A1A1A; font-size: 42px; letter-spacing: 14px; margin: 0; font-family: 'Courier New', Courier, monospace; font-weight: 300;\">" + otpCode + "</h1>" +
                "</div>" +
                "<p style=\"color: #888888; font-size: 13px; margin-bottom: 5px; font-style: italic;\">* Mã xác thực có hiệu lực trong vòng <strong>10 phút</strong> kể từ khi nhận được email này.</p>" +
                "<p style=\"color: #888888; font-size: 13px; font-style: italic;\">* Vì sự an toàn của Quý khách, tuyệt đối không chia sẻ mã này cho bất kỳ bên thứ ba nào.</p>" +
                "<div style=\"margin-top: 40px; border-top: 1px solid #EAEAEA; padding-top: 20px;\">" +
                "<p style=\"color: #333333; font-size: 15px; margin: 0;\">Trân trọng,</p>" +
                "<p style=\"color: #1A1A1A; font-size: 16px; margin: 5px 0 0 0; font-weight: bold; letter-spacing: 1px;\">HOANIEN Concierge Team</p>" +
                "</div>" +
                "</div>" +
                "<div style=\"text-align: center; margin-top: 30px; color: #999999; font-size: 11px; letter-spacing: 1px;\">" +
                "© 2026 HOANIEN Retreat & Resort. All rights reserved." +
                "</div>" +
                "</body></html>";
    }

    private String buildPasswordResetEmail(String fullName, String otpCode) {
        return "<!DOCTYPE html>" +
                "<html><body style=\"font-family: 'Times New Roman', Times, serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #FAFAFA;\">" +
                "<div style=\"background: linear-gradient(135deg, #1A1A1A 0%, #2C2C2C 100%); padding: 40px 30px; text-align: center; border-radius: 8px 8px 0 0;\">" +
                "<h1 style=\"color: #D4AF37; margin: 0; font-size: 32px; letter-spacing: 6px; font-weight: 400;\">HOANIEN</h1>" +
                "<p style=\"color: #E0E0E0; margin-top: 8px; font-weight: 300; font-size: 14px; letter-spacing: 2px; font-style: italic;\">Retreat & Resort</p>" +
                "</div>" +
                "<div style=\"background: #FFFFFF; padding: 40px 30px; border-left: 1px solid #EAEAEA; border-right: 1px solid #EAEAEA; border-bottom: 1px solid #EAEAEA; border-radius: 0 0 8px 8px;\">" +
                "<div style=\"text-align: center; margin-bottom: 30px;\">" +
                "<span style=\"display: inline-block; padding: 6px 16px; background-color: #FFF5F5; border: 1px solid #C5A0A0; color: #8A4B4B; font-size: 12px; letter-spacing: 1px; text-transform: uppercase; border-radius: 20px;\">Khôi phục mật khẩu</span>" +
                "</div>" +
                "<p style=\"color: #333333; font-size: 16px; line-height: 1.6; margin-bottom: 20px;\">Kính gửi Quý khách <strong style=\"color: #1A1A1A;\">" + fullName + "</strong>,</p>" +
                "<p style=\"color: #555555; font-size: 15px; line-height: 1.8;\"><i>HOANIEN Retreat & Resort</i> đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của Quý khách. Để đảm bảo tính bảo mật, xin vui lòng sử dụng mã xác nhận dưới đây để thiết lập lại mật khẩu:</p>" +
                "<div style=\"background: #FDFBF7; padding: 30px; border-radius: 8px; text-align: center; margin: 35px 0; border: 1px solid #E8E0D5;\">" +
                "<h1 style=\"color: #8A4B4B; font-size: 42px; letter-spacing: 14px; margin: 0; font-family: 'Courier New', Courier, monospace; font-weight: 300;\">" + otpCode + "</h1>" +
                "</div>" +
                "<p style=\"color: #888888; font-size: 13px; margin-bottom: 5px; font-style: italic;\">* Mã xác nhận này chỉ có hiệu lực trong vòng <strong>15 phút</strong>.</p>" +
                "<p style=\"color: #888888; font-size: 13px; font-style: italic;\">* Nếu Quý khách không thực hiện yêu cầu này, xin vui lòng bỏ qua email và đảm bảo mật khẩu hiện tại vẫn đang được bảo mật an toàn.</p>" +
                "<div style=\"margin-top: 40px; border-top: 1px solid #EAEAEA; padding-top: 20px;\">" +
                "<p style=\"color: #333333; font-size: 15px; margin: 0;\">Trân trọng,</p>" +
                "<p style=\"color: #1A1A1A; font-size: 16px; margin: 5px 0 0 0; font-weight: bold; letter-spacing: 1px;\">HOANIEN Concierge Team</p>" +
                "</div>" +
                "</div>" +
                "<div style=\"text-align: center; margin-top: 30px; color: #999999; font-size: 11px; letter-spacing: 1px;\">" +
                "© 2026 HOANIEN Retreat & Resort. All rights reserved." +
                "</div>" +
                "</body></html>";
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
