package com.kawai.controllers.api;

import com.kawai.services.interfaces.NightAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
     * Endpoint: GET /api/v1/audit/folio/{id}/balance
     * Lấy tổng dư nợ của Folio.
     */
    @GetMapping("/folio/{id}/balance")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getFolioBalance(@PathVariable("id") Long id) {
        java.math.BigDecimal balance = nightAuditService.calculateFolioBalance(id);
        Map<String, Object> response = new HashMap<>();
        response.put("balance", balance);
        return ResponseEntity.ok(response);
    }
}
