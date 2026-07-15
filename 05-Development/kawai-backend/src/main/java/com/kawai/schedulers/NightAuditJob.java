package com.kawai.schedulers;

import com.kawai.services.interfaces.NightAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class NightAuditJob {

    private static final Logger logger = LoggerFactory.getLogger(NightAuditJob.class);

    private final NightAuditService nightAuditService;

    @Autowired
    public NightAuditJob(NightAuditService nightAuditService) {
        this.nightAuditService = nightAuditService;
    }

    public void runAutomaticNightAudit() {
        logger.info("Bắt đầu tiến trình Night Audit tự động chạy ngầm...");
        LocalDate today = LocalDate.now();
        nightAuditService.runNightAudit(today);
        logger.info("Tiến trình Night Audit tự động hoàn tất thành công cho ngày {}", today);
    }
}
