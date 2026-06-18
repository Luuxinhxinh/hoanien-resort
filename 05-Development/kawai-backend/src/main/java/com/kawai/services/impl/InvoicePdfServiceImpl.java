package com.kawai.services.impl;

import com.kawai.models.ConsolidatedInvoice;
import com.kawai.services.interfaces.InvoicePdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private static final Logger logger = LoggerFactory.getLogger(InvoicePdfServiceImpl.class);

    @Override
    public String generateInvoicePdf(ConsolidatedInvoice invoice) {
        if (!"Paid".equalsIgnoreCase(invoice.getInvoiceStatus())) {
            throw new IllegalStateException("Không thể xuất PDF cho hóa đơn chưa được thanh toán (Trạng thái hiện tại: " + invoice.getInvoiceStatus() + ")");
        }
        
        // Mô phỏng việc tạo file PDF
        // Nếu muốn tạo thật, có thể dùng iTextPDF hoặc OpenPDF ở đây
        String fakeFilePath = "/storage/invoices/" + invoice.getInvoiceNumber() + ".pdf";
        logger.info("[PDF SERVICE] Đang render dữ liệu hóa đơn {} thành file PDF...", invoice.getInvoiceNumber());
        logger.info("[PDF SERVICE] Sinh file thành công tại: {}", fakeFilePath);
        return fakeFilePath;
    }
}
