package com.kawai.services.jobs;

import com.kawai.models.RoomBooking;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class RoomBookingCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(RoomBookingCleanupTask.class);
    private final RoomBookingRepository roomBookingRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;

    public RoomBookingCleanupTask(
            RoomBookingRepository roomBookingRepository,
            RoomBookingDetailRepository roomBookingDetailRepository) {
        this.roomBookingRepository = roomBookingRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
    }

    // Quản lý qua DynamicJobManager và tự động chạy khi khởi động app
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cleanupNoShowRoomBookings() {
        LocalDate today = LocalDate.now();

        // 1. Confirmed bookings past check-in -> No_Show
        List<RoomBooking> confirmedExpired = roomBookingRepository.findByBookingStatusAndCheckInDateBefore("Confirmed",
                today);
        if (!confirmedExpired.isEmpty()) {
            log.info("Bắt đầu giải phóng {} đơn phòng Confirmed quá hạn sang No_Show.", confirmedExpired.size());
            for (RoomBooking rb : confirmedExpired) {
                rb.setBookingStatus("No_Show");
                roomBookingRepository.save(rb);

                List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(rb.getId());
                for (RoomBookingDetail d : details) {
                    d.setDetailStatus("No_Show");
                    roomBookingDetailRepository.save(d);
                }
            }
        }

        // 2. Pending bookings past check-in -> Cancelled
        List<RoomBooking> pendingExpired = roomBookingRepository.findByBookingStatusAndCheckInDateBefore("Pending",
                today);
        if (!pendingExpired.isEmpty()) {
            log.info("Bắt đầu giải phóng {} đơn phòng Pending quá hạn sang Cancelled.", pendingExpired.size());
            for (RoomBooking rb : pendingExpired) {
                rb.setBookingStatus("Cancelled");
                roomBookingRepository.save(rb);

                List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(rb.getId());
                for (RoomBookingDetail d : details) {
                    d.setDetailStatus("Cancelled");
                    roomBookingDetailRepository.save(d);
                }
            }
        }

        // 3. Pending_Approval bookings past check-in -> Cancelled
        List<RoomBooking> pendingAppExpired = roomBookingRepository
                .findByBookingStatusAndCheckInDateBefore("Pending_Approval", today);
        if (!pendingAppExpired.isEmpty()) {
            log.info("Bắt đầu giải phóng {} đơn phòng Pending_Approval quá hạn sang Cancelled.",
                    pendingAppExpired.size());
            for (RoomBooking rb : pendingAppExpired) {
                rb.setBookingStatus("Cancelled");
                roomBookingRepository.save(rb);

                List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(rb.getId());
                for (RoomBookingDetail d : details) {
                    d.setDetailStatus("Cancelled");
                    roomBookingDetailRepository.save(d);
                }
            }
        }
    }
}
