package com.kawai.services.interfaces;

import com.kawai.models.ConsolidatedInvoice;

public interface EmailService {
    /**
     * Gửi email đính kèm hóa đơn PDF cho khách hàng
     * @param toEmail Địa chỉ email người nhận
     * @param invoice Thông tin hóa đơn
     * @param pdfAttachmentPath Đường dẫn tới file PDF
     */
    void sendInvoiceEmail(String toEmail, ConsolidatedInvoice invoice, String pdfAttachmentPath);
}
