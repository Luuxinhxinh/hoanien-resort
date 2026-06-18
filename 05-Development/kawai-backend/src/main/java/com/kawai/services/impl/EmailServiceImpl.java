package com.kawai.services.impl;

import com.kawai.models.ConsolidatedInvoice;
import com.kawai.services.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendInvoiceEmail(String toEmail, ConsolidatedInvoice invoice, String pdfAttachmentPath) {
        if (!"Paid".equalsIgnoreCase(invoice.getInvoiceStatus())) {
            throw new IllegalStateException("Không thể gửi Email cho hóa đơn chưa được thanh toán (Trạng thái hiện tại: " + invoice.getInvoiceStatus() + ")");
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
}
