package com.hoanien.services.interfaces;

import com.hoanien.models.FolioItem;
import java.math.BigDecimal;

public interface FolioService {
    void addFolioItem(Long bookingDetailId, String department, BigDecimal amount, String description);
    BigDecimal getFolioBalance(Long bookingDetailId);
    void performNightAudit(Long staffId);
    void checkOutAndSettle(Long bookingDetailId, String paymentMethod);
}
