// package com.kawai.controllers.api;

import com.kawai.services.interfaces.NightAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller API cho tiến trình Night Audit (UC24).
 */
@RestController
@RequestMapping("/api/v1/audit")
public class NightAuditRestController {

    private final NightAuditService nightAuditService;

    @Autowired
    public NightAuditRestController(NightAuditService nightAuditService) {
        this.nightAuditService = nightAuditService;
    }

    /**
     * Endpoint: POST /api/v1/audit/night-audit
     * Kích hoạt thủ công tiến trình Night Audit.
     */
    @PostMapping("/night-audit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SYSTEM')")
    public ResponseEntity<Map<String, Object>> runNightAudit() {
        LocalDate today = LocalDate.now();
        try {
            nightAuditService.runNightAudit(today);
            LocalDate nextDay = nightAuditService.getNextBusinessDate(today);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("businessDate", nextDay.toString());
            response.put("message", "Night Audit hoàn tất thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "SYS-001: Lỗi hệ thống nội bộ - " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
