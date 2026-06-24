package com.kawai.controllers.api;

import com.kawai.config.DynamicJobManager;
import com.kawai.services.AuditCleanupTask;
import com.kawai.services.jobs.ReservationCleanupTask;
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
        List<Map<String, Object>> jobs = new ArrayList<>();

        Map<String, Object> job1 = new HashMap<>();
        job1.put("id", "audit_cleanup");
        job1.put("name", "Dọn dẹp Audit Log cũ (Hơn 90 ngày)");
        job1.put("schedule", dynamicJobManager.getAuditCron());
        job1.put("description", "Tự động xóa các bản ghi lịch sử kiểm toán quá cũ để tối ưu không gian Database.");
        job1.put("status", "ACTIVE");
        job1.put("lastRun", auditCleanupTask.getLastRunTime());
        jobs.add(job1);

        Map<String, Object> job2 = new HashMap<>();
        job2.put("id", "reservation_cleanup");
        job2.put("name", "Hủy đặt bàn F&B quá giờ (No-show)");
        job2.put("schedule", dynamicJobManager.getReservationCron());
        job2.put("description", "Tự động quét và hủy các lượt đặt bàn nhà hàng đã quá hạn 30 phút mà khách không đến.");
        job2.put("status", "ACTIVE");
        job2.put("lastRun", reservationCleanupTask.getLastRunTime());
        jobs.add(job2);

        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, String>> runJobNow(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        try {
            if ("audit_cleanup".equals(id)) {
                auditCleanupTask.cleanupOldAuditLogs();
                response.put("message", "Đã thực thi thủ công tác vụ dọn dẹp Audit Log thành công!");
            } else if ("reservation_cleanup".equals(id)) {
                reservationCleanupTask.cleanupNoShowReservations();
                response.put("message", "Đã thực thi thủ công tác vụ hủy bàn No-show thành công!");
            } else {
                response.put("error", "Không tìm thấy tiến trình.");
                return ResponseEntity.badRequest().body(response);
            }
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

            if ("audit_cleanup".equals(id)) {
                dynamicJobManager.scheduleAuditTask(cron);
                response.put("message", "Cập nhật lịch cho tác vụ Audit thành công!");
            } else if ("reservation_cleanup".equals(id)) {
                dynamicJobManager.scheduleReservationTask(cron);
                response.put("message", "Cập nhật lịch cho tác vụ No-show thành công!");
            } else {
                response.put("error", "Không tìm thấy tiến trình.");
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ie) {
            response.put("error", "Định dạng Cron không hợp lệ: " + ie.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Lỗi khi cập nhật cấu hình: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
