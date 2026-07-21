package com.kawai.services.interfaces;

import com.kawai.dto.fnb.FnBDailyReportPreviewResponse;
import com.kawai.models.FnBDailyReport;

import java.time.LocalDate;

public interface FnBDailyReportService {
    FnBDailyReportPreviewResponse previewDailyReport(LocalDate date, Long staffId);
    FnBDailyReport closeDailyReport(LocalDate date, Long staffId, String notes);
}
