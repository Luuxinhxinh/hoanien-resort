package com.kawai.config;

import com.kawai.services.AuditCleanupTask;
import com.kawai.services.impl.BookingServiceImpl;
import com.kawai.services.impl.WorkflowEngineServiceImpl;
import com.kawai.services.jobs.ReservationCleanupTask;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableScheduling
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicJobManager {

    private final TaskScheduler taskScheduler;
    private final AuditCleanupTask auditCleanupTask;
    private final ReservationCleanupTask reservationCleanupTask;
    private final BookingServiceImpl bookingService;
    private final WorkflowEngineServiceImpl workflowEngineService;

    public static class JobConfig {
        public String id;
        public String name;
        public String description;
        public String cron;
        public boolean isPaused = false;
        public ScheduledFuture<?> future;
        public Runnable task;
        public Deque<Map<String, Object>> logs = new ConcurrentLinkedDeque<>();

        public JobConfig(String id, String name, String desc, String cron, Runnable task) {
            this.id = id;
            this.name = name;
            this.description = desc;
            this.cron = cron;
            this.task = task;
        }
    }

    private final Map<String, JobConfig> jobs = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        jobs.put("audit_cleanup", new JobConfig("audit_cleanup", "Dọn dẹp Audit Log cũ (> 90 ngày)", "Tự động xóa các bản ghi lịch sử kiểm toán quá cũ để tối ưu Database.", "0 0 2 * * ?", auditCleanupTask::cleanupOldAuditLogs));
        jobs.put("reservation_cleanup", new JobConfig("reservation_cleanup", "Hủy đặt bàn F&B quá giờ (No-show)", "Tự động quét và hủy các lượt đặt bàn nhà hàng đã quá hạn 30 phút mà khách không đến.", "0 0/15 * * * ?", reservationCleanupTask::cleanupNoShowReservations));
        jobs.put("table_cleanup", new JobConfig("table_cleanup", "Dọn bàn F&B tự động", "Tự động cập nhật trạng thái bàn sang trống sau thời gian dọn dẹp.", "0 * * * * *", reservationCleanupTask::cleanupTables));
        jobs.put("booking_cleanup", new JobConfig("booking_cleanup", "Hủy Booking Pending quá hạn", "Quét các Booking chờ thanh toán (Pending_Payment) quá hạn VNPay để giải phóng phòng.", "0 * * * * *", bookingService::cleanupStaleHolds));
        jobs.put("workflow_processor", new JobConfig("workflow_processor", "Xử lý Workflow tự động", "Quét và thực thi tự động các sự kiện Workflow Engine (VD: Duyệt chiết khấu).", "0 */1 * * * *", workflowEngineService::scanSlaEscalations));

        for (JobConfig config : jobs.values()) {
            scheduleJob(config.id, config.cron);
        }
    }

    public synchronized void scheduleJob(String id, String cronExpression) {
        JobConfig config = jobs.get(id);
        if (config == null) return;
        
        if (config.future != null) {
            config.future.cancel(false);
        }
        config.cron = cronExpression;
        
        if (!config.isPaused) {
            config.future = taskScheduler.schedule(() -> executeWithLog(id), new CronTrigger(cronExpression));
            log.info("Job {} scheduled with cron: {}", id, cronExpression);
        } else {
            log.info("Job {} is paused, skipped scheduling.", id);
        }
    }

    public void executeWithLog(String id) {
        JobConfig config = jobs.get(id);
        if (config == null || config.isPaused) return;

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("startTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        long startMs = System.currentTimeMillis();
        
        try {
            config.task.run();
            logEntry.put("status", "SUCCESS");
            logEntry.put("output", "Thành công");
        } catch (Exception e) {
            logEntry.put("status", "ERROR");
            logEntry.put("output", e.getMessage());
            log.error("Job {} failed: {}", id, e.getMessage());
        } finally {
            long duration = System.currentTimeMillis() - startMs;
            logEntry.put("durationMs", duration);
            logEntry.put("endTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            config.logs.addFirst(logEntry);
            if (config.logs.size() > 10) {
                config.logs.removeLast();
            }
        }
    }

    public void togglePause(String id) {
        JobConfig config = jobs.get(id);
        if (config != null) {
            config.isPaused = !config.isPaused;
            scheduleJob(id, config.cron); 
        }
    }

    public List<Map<String, Object>> getAllJobsInfo() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (JobConfig config : jobs.values()) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", config.id);
            info.put("name", config.name);
            info.put("description", config.description);
            info.put("schedule", config.cron);
            info.put("status", config.isPaused ? "PAUSED" : "ACTIVE");
            info.put("lastRun", config.logs.isEmpty() ? "Chưa chạy" : config.logs.peekFirst().get("startTime"));
            result.add(info);
        }
        return result;
    }
    
    public List<Map<String, Object>> getJobLogs(String id) {
        JobConfig config = jobs.get(id);
        if (config != null) {
            return new ArrayList<>(config.logs);
        }
        return new ArrayList<>();
    }
}
