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

    @Scheduled(fixedRate = 900000) // 15 minutes
    public void cleanupNoShowReservations() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<TableReservation> pendingReservations = tableReservationRepository.findByReserveDate(today).stream()
            .filter(res -> "Confirmed".equalsIgnoreCase(res.getStatus()) || "Pending".equalsIgnoreCase(res.getStatus()))
            .filter(res -> res.getReserveTime() != null && res.getReserveTime().plusMinutes(30).isBefore(now))
            .collect(Collectors.toList());
            
        for (TableReservation res : pendingReservations) {
            res.setStatus("Cancelled");
            res.setSpecialRequests((res.getSpecialRequests() != null ? res.getSpecialRequests() : "") + " [System: Auto-cancelled due to No-show]");
            tableReservationRepository.save(res);
        }
    }
}
