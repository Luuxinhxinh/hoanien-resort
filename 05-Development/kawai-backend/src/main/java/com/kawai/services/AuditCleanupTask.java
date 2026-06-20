package com.kawai.services;

import com.kawai.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditCleanupTask {

    private final AuditLogRepository auditLogRepository;

    /**
     * Chạy vào lúc 2:00 sáng mỗi ngày.
     * Xóa tất cả các bản ghi AuditLog cũ hơn 90 ngày để tránh đầy Database.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldAuditLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        log.info("Bắt đầu dọn dẹp các bản ghi AuditLog cũ hơn: {}", cutoffDate);
        
        try {
            auditLogRepository.deleteByTimestampBefore(cutoffDate);
            log.info("Dọn dẹp AuditLog thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi dọn dẹp AuditLog: ", e);
        }
    }
}
