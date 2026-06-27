package com.kawai.services.jobs;

import com.kawai.models.TableReservation;
import com.kawai.repositories.TableReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import com.kawai.models.RestaurantTable;
import com.kawai.repositories.RestaurantTableRepository;

@Component
public class ReservationCleanupTask {

    private final TableReservationRepository tableReservationRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final com.kawai.services.interfaces.EmailService emailService;

    public ReservationCleanupTask(TableReservationRepository tableReservationRepository, RestaurantTableRepository restaurantTableRepository, com.kawai.services.interfaces.EmailService emailService) {
        this.tableReservationRepository = tableReservationRepository;
        this.restaurantTableRepository = restaurantTableRepository;
        this.emailService = emailService;
    }

    private java.time.LocalDateTime lastRun;

    public String getLastRunTime() {
        return lastRun != null ? java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(lastRun) : "Chưa chạy lần nào";
    }

    public void cleanupTables() {
        List<RestaurantTable> cleaningTables = restaurantTableRepository.findAll().stream()
                .filter(t -> "Cleaning".equalsIgnoreCase(t.getTableStatus()) && t.getCleaningStartTime() != null)
                .collect(Collectors.toList());

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (RestaurantTable table : cleaningTables) {
            if (table.getCleaningStartTime().plusMinutes(10).isBefore(now)) {
                table.setTableStatus("Available");
                table.setCleaningStartTime(null);
                restaurantTableRepository.save(table);
            }
        }
    }

    // Scheduled dynamically in DynamicJobConfig
    public void cleanupNoShowReservations() {
        this.lastRun = java.time.LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<TableReservation> pendingReservations = tableReservationRepository.findByReserveDate(today).stream()
            .filter(res -> "Confirmed".equalsIgnoreCase(res.getStatus()) || "Pending".equalsIgnoreCase(res.getStatus()))
            .filter(res -> {
                if (res.getReserveTime() == null) return false;
                int holdMinutes = 0;
                String notes = res.getSpecialRequests();
                if (notes != null) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[HELD: (\\d+)m\\]").matcher(notes);
                    if (m.find()) {
                        try {
                            holdMinutes = Integer.parseInt(m.group(1));
                        } catch (Exception ignored) {}
                    }
                }
                return res.getReserveTime().plusMinutes(15 + holdMinutes).isBefore(now);
            })
            .collect(Collectors.toList());
            
        for (TableReservation res : pendingReservations) {
            res.setStatus("Cancelled");
            res.setSpecialRequests((res.getSpecialRequests() != null ? res.getSpecialRequests() : "") + " [System: Auto-cancelled due to No-show]");
            TableReservation savedRes = tableReservationRepository.save(res);
            
            if (savedRes.getCustomer() != null) {
                emailService.sendCancelTableBooking(savedRes, savedRes.getCustomer());
            }
        }
    }
}
