package com.kawai.services.interfaces;

import com.kawai.models.ConsolidatedInvoice;

public interface InvoicePdfService {
    /**
     * Sinh file PDF cho hóa đơn tổng hợp
     * @param invoice Hóa đơn đã được chốt (trạng thái Paid)
     * @return File path hoặc URL của file PDF vừa sinh
     */
    String generateInvoicePdf(ConsolidatedInvoice invoice);
}
