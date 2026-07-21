package com.kawai.controllers.api;

import com.kawai.config.DynamicJobManager;
import com.kawai.services.impl.AuditCleanupTask;
import com.kawai.services.impl.ReservationCleanupTask;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/v1/cronjobs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class CronjobApiController {

    private final AuditCleanupTask auditCleanupTask;
    private final ReservationCleanupTask reservationCleanupTask;
    private final DynamicJobManager dynamicJobManager;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCronjobs() {
        return ResponseEntity.ok(dynamicJobManager.getAllJobsInfo());
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, String>> runJobNow(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            dynamicJobManager.executeWithLog(id);
            response.put("message", "Đã thực thi thủ công tác vụ thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Lỗi khi chạy tiến trình: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/{id}/config")
    public ResponseEntity<Map<String, String>> updateJobConfig(@PathVariable String id, @RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        try {
            String cron = request.get("cron");
            if (cron == null || cron.trim().isEmpty()) {
                response.put("error", "Cấu hình Cron không được để trống.");
                return ResponseEntity.badRequest().body(response);
            }
            // Basic cron validation (simplistic)
            if (cron.split("\\s+").length < 6) {
                response.put("error", "Định dạng Cron không hợp lệ.");
                return ResponseEntity.badRequest().body(response);
            }

            dynamicJobManager.scheduleJob(id, cron);
            response.put("message", "Cập nhật lịch cho tác vụ thành công!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ie) {
            response.put("error", "Định dạng Cron không hợp lệ: " + ie.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Lỗi khi cập nhật cấu hình: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PostMapping("/{id}/toggle")
    public ResponseEntity<Map<String, String>> toggleJob(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            dynamicJobManager.togglePause(id);
            response.put("message", "Đã thay đổi trạng thái tiến trình!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<Map<String, Object>>> getJobLogs(@PathVariable String id) {
        return ResponseEntity.ok(dynamicJobManager.getJobLogs(id));
    }
}
