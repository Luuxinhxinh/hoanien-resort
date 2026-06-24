package com.kawai.services.interfaces;

import com.kawai.models.ConsolidatedInvoice;

public interface EmailService {
    /**
     * Gửi email đính kèm hóa đơn PDF cho khách hàng
     * 
     * @param toEmail           Địa chỉ email người nhận
     * @param invoice           Thông tin hóa đơn
     * @param pdfAttachmentPath Đường dẫn tới file PDF
     */
    void sendInvoiceEmail(String toEmail, ConsolidatedInvoice invoice, String pdfAttachmentPath);

    /**
     * Gửi email OTP xác nhận đăng ký tài khoản
     * 
     * @param toEmail  Địa chỉ email người nhận
     * @param otpCode  Mã OTP 6 số
     * @param fullName Tên người dùng
     */
    void sendRegistrationOtpEmail(String toEmail, String otpCode, String fullName);

    /**
     * Gửi email chứa link đặt lại mật khẩu
     * 
     * @param toEmail   Địa chỉ email người nhận
     * @param resetLink Link đặt lại mật khẩu
     * @param fullName  Tên người dùng
     */
    void sendPasswordResetEmail(String toEmail, String resetLink, String fullName);

    /**
     * Gửi email cảnh báo SLA cho supervisor
     */
    void sendSlaWarningEmail(String toEmail, String taskName, int pendingMinutes, String roomNumber);

    /**
     * Gửi email tùy chỉnh từ Workflow Engine
     */
    void sendCustomWorkflowEmail(String fromEmail, String toEmail, String subject, String htmlContent);
}
