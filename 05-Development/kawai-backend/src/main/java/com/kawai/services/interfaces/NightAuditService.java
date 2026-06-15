package com.kawai.services.interfaces;

import com.kawai.models.FolioItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface cho dịch vụ Night Audit và tính toán Folio (UC24).
 */
public interface NightAuditService {
    
    /**
     * Lấy danh sách nợ (FolioItem) của một phòng chưa thanh toán.
     */
    List<FolioItem> getFolioItems(Long roomBookingDetailId);

    /**
     * Tính tổng số nợ hiện tại của phòng (Folio Balance).
     */
    BigDecimal calculateFolioBalance(Long roomBookingDetailId);

    /**
     * Tính tổng tất cả giao dịch (gồm cả đã thanh toán) của phòng.
     */
    BigDecimal aggregateFolioTotal(Long roomBookingDetailId);

    /**
     * Chạy tiến trình Night Audit.
     * Cộng phí phòng cho các phòng đang OCCUPIED.
     */
    void runNightAudit(LocalDate auditDate);

    /**
     * Lấy Business Date tiếp theo sau khi chốt sổ.
     */
    LocalDate getNextBusinessDate(LocalDate auditDate);
}
