package com.kawai.services.jobs;

import com.kawai.models.RoomBooking;
import com.kawai.repositories.RoomBookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class RoomBookingCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(RoomBookingCleanupTask.class);
    private final RoomBookingRepository roomBookingRepository;

    public RoomBookingCleanupTask(RoomBookingRepository roomBookingRepository) {
        this.roomBookingRepository = roomBookingRepository;
    }

    // Chạy vào 00:01 mỗi ngày
    @Scheduled(cron = "0 1 0 * * ?")
    @Transactional
    public void cleanupNoShowRoomBookings() {
        LocalDate today = LocalDate.now();
        
        // Tìm tất cả các đơn 'Confirmed' mà ngày checkIn < hôm nay
        List<RoomBooking> expiredBookings = roomBookingRepository.findByBookingStatusAndCheckInDateBefore("Confirmed", today);
        
        if (!expiredBookings.isEmpty()) {
            log.info("Bắt đầu giải phóng {} đơn phòng quá hạn (No-Show).", expiredBookings.size());
            
            for (RoomBooking rb : expiredBookings) {
                rb.setBookingStatus("No-Show");
                roomBookingRepository.save(rb);
            }
            
            log.info("Hoàn tất giải phóng các đơn phòng quá hạn.");
        }
    }
}
