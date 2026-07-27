package com.hoanien.services.interfaces;

import com.hoanien.dto.fnb.FnBDailyReportPreviewResponse;
import com.hoanien.models.FnBDailyReport;

import java.time.LocalDate;

public interface FnBDailyReportService {
    FnBDailyReportPreviewResponse previewDailyReport(LocalDate date, Long staffId);
    FnBDailyReport closeDailyReport(LocalDate date, Long staffId, String notes);
}
