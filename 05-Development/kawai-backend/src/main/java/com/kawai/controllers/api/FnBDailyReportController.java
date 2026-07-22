package com.kawai.controllers.api;

import com.kawai.dto.fnb.FnBDailyReportPreviewResponse;
import com.kawai.models.FnBDailyReport;
import com.kawai.repositories.FnBDailyReportRepository;
import com.kawai.services.interfaces.FnBDailyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fnb/daily-reports")
@RequiredArgsConstructor
public class FnBDailyReportController {

    private final FnBDailyReportService fnBDailyReportService;
    private final FnBDailyReportRepository fnBDailyReportRepository;

    @GetMapping("/preview")
    public ResponseEntity<FnBDailyReportPreviewResponse> previewReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long staffId) {
        return ResponseEntity.ok(fnBDailyReportService.previewDailyReport(date, staffId));
    }

    @PostMapping("/close")
    public ResponseEntity<FnBDailyReport> closeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long staffId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(fnBDailyReportService.closeDailyReport(date, staffId, notes));
    }

    @GetMapping
    public ResponseEntity<List<FnBDailyReport>> getAllReports() {
        return ResponseEntity.ok(fnBDailyReportRepository.findAll());
    }
}
