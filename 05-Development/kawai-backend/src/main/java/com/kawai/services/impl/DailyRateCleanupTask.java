package com.kawai.services.impl;

import com.kawai.repositories.DailyRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class DailyRateCleanupTask {

    @Autowired
    private DailyRateRepository dailyRateRepository;

    // Run at 00:05:00 every day
    @Scheduled(cron = "0 5 0 * * ?")
    public void cleanupOldRates() {
        log.info("Starting DailyRateCleanupTask scheduler to clean up past rate records...");
        try {
            LocalDate cutOffDate = LocalDate.now().minusDays(30);
            dailyRateRepository.deleteByRateDateBefore(cutOffDate);
            log.info("DailyRateCleanupTask completed successfully. All rates before {} have been purged.", cutOffDate);
        } catch (Exception e) {
            log.error("Error occurred during DailyRateCleanupTask execution: ", e);
        }
    }
}
