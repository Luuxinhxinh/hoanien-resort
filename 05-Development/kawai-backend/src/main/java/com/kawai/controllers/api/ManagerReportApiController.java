package com.kawai.controllers.api;

import com.kawai.models.ExportHistory;
import com.kawai.repositories.ExportHistoryRepository;
import com.kawai.services.interfaces.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/manager/api/reports")
public class ManagerReportApiController {

    private final ReportService reportService;
    private final ExportHistoryRepository exportHistoryRepository;

    public ManagerReportApiController(ReportService reportService, ExportHistoryRepository exportHistoryRepository) {
        this.reportService = reportService;
        this.exportHistoryRepository = exportHistoryRepository;
    }

    @GetMapping("/usali")
    public ResponseEntity<?> usali(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body(Map.of("message", "from must be before or equal to to"));
        }
        return ResponseEntity.ok(reportService.getUsaliRevenueReport(from, to));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "usali") String type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "csv") String format) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body("Invalid date range".getBytes());
        }

        byte[] content = "usali".equalsIgnoreCase(type) || "revenue".equalsIgnoreCase(type)
                ? reportService.exportUsaliReport(from, to, format)
                : reportService.exportUsaliReport(from, to, format);

        ExportHistory history = new ExportHistory();
        history.setReportName(("usali".equalsIgnoreCase(type) ? "USALI" : type.toUpperCase()) + " " + from + " - " + to);
        history.setFormat(format.toUpperCase());
        history.setExportedAt(LocalDateTime.now());
        history.setExportedBy("Manager");
        history.setFileSize(content.length + " bytes");
        exportHistoryRepository.save(history);

        String extension = "xlsx".equalsIgnoreCase(format) ? "csv" : format.toLowerCase();
        String filename = "usali-report-" + from + "-" + to + "." + extension;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }
}
