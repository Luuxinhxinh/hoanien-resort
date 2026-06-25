package com.kawai.services.jobs;

import com.kawai.models.TableReservation;
import com.kawai.repositories.TableReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationCleanupTask {

    private final TableReservationRepository tableReservationRepository;

    public ReservationCleanupTask(TableReservationRepository tableReservationRepository) {
        this.tableReservationRepository = tableReservationRepository;
    }

    private java.time.LocalDateTime lastRun;

    public String getLastRunTime() {
        return lastRun != null ? java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(lastRun) : "Chưa chạy lần nào";
    }

    // Scheduled dynamically in DynamicJobConfig
    public void cleanupNoShowReservations() {
        this.lastRun = java.time.LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<TableReservation> pendingReservations = tableReservationRepository.findByReserveDate(today).stream()
            .filter(res -> "Confirmed".equalsIgnoreCase(res.getStatus()) || "Pending".equalsIgnoreCase(res.getStatus()))
            .filter(res -> res.getReserveTime() != null && res.getReserveTime().plusMinutes(15).isBefore(now))
            .collect(Collectors.toList());
            
        for (TableReservation res : pendingReservations) {
            res.setStatus("Cancelled");
            res.setSpecialRequests((res.getSpecialRequests() != null ? res.getSpecialRequests() : "") + " [System: Auto-cancelled due to No-show]");
            tableReservationRepository.save(res);
        }
    }
}
