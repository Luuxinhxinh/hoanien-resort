package com.kawai.config;

import com.kawai.services.AuditCleanupTask;
import com.kawai.services.jobs.ReservationCleanupTask;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Configuration
@EnableScheduling
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicJobManager {

    private final TaskScheduler taskScheduler;
    private final AuditCleanupTask auditCleanupTask;
    private final ReservationCleanupTask reservationCleanupTask;

    private String auditCron = "0 0 2 * * ?";
    private String reservationCron = "0 0/15 * * * ?";

    private ScheduledFuture<?> auditFuture;
    private ScheduledFuture<?> reservationFuture;

    @PostConstruct
    public void init() {
        scheduleAuditTask(auditCron);
        scheduleReservationTask(reservationCron);
    }

    public synchronized void scheduleAuditTask(String cronExpression) {
        if (auditFuture != null) {
            auditFuture.cancel(false);
        }
        this.auditCron = cronExpression;
        auditFuture = taskScheduler.schedule(auditCleanupTask::cleanupOldAuditLogs, new CronTrigger(cronExpression));
        log.info("AuditCleanupTask rescheduled with cron: {}", cronExpression);
    }

    public synchronized void scheduleReservationTask(String cronExpression) {
        if (reservationFuture != null) {
            reservationFuture.cancel(false);
        }
        this.reservationCron = cronExpression;
        reservationFuture = taskScheduler.schedule(reservationCleanupTask::cleanupNoShowReservations, new CronTrigger(cronExpression));
        log.info("ReservationCleanupTask rescheduled with cron: {}", cronExpression);
    }

    public String getAuditCron() {
        return auditCron;
    }

    public String getReservationCron() {
        return reservationCron;
    }
}
