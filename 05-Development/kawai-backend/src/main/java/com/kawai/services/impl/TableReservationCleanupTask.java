package com.kawai.services.impl;

import com.kawai.models.TableReservation;
import com.kawai.models.Customer;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.services.interfaces.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Component
public class TableReservationCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(TableReservationCleanupTask.class);

    private final TableReservationRepository tableReservationRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final EmailService emailService;

    public TableReservationCleanupTask(TableReservationRepository tableReservationRepository,
                                       RoomBookingRepository roomBookingRepository,
                                       EmailService emailService) {
        this.tableReservationRepository = tableReservationRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.emailService = emailService;
    }

    /**
     * Run every hour.
     * Checks if future table reservations are still backed by a valid RoomBooking.
     * If the customer checked out early or cancelled their room, cancel the table reservation.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupInvalidTableReservations() {
        logger.info("Bắt đầu quét dọn các đơn đặt bàn không còn lịch lưu trú hợp lệ...");
        LocalDate today = LocalDate.now();
        
        List<TableReservation> pendingReservations = tableReservationRepository.findAll().stream()
                .filter(r -> !r.getReserveDate().isBefore(today))
                .filter(r -> "Pending".equalsIgnoreCase(r.getStatus()) || "Confirmed".equalsIgnoreCase(r.getStatus()))
                .toList();

        int canceledCount = 0;

        for (TableReservation reservation : pendingReservations) {
            Customer customer = reservation.getCustomer();
            if (customer == null) continue;

            LocalDate reserveDate = reservation.getReserveDate();
            List<com.kawai.models.RoomBooking> rbs = roomBookingRepository.findByCustomerOrderByBookingDateDesc(customer);
            boolean hasValidBooking = false;
            
            for (com.kawai.models.RoomBooking rb : rbs) {
                if (("Checked_In".equals(rb.getBookingStatus()) || "Confirmed".equals(rb.getBookingStatus()))
                        && !rb.getCheckOutDate().isBefore(today)) {
                    
                    LocalDate checkIn = rb.getCheckInDate();
                    if (checkIn.isBefore(today)) checkIn = today; 
                    
                    if (!reserveDate.isBefore(checkIn) && !reserveDate.isAfter(rb.getCheckOutDate())) {
                        hasValidBooking = true;
                        break;
                    }
                }
            }

            if (!hasValidBooking) {
                // Cancel the reservation
                reservation.setStatus("Canceled");
                tableReservationRepository.save(reservation);
                canceledCount++;
                logger.info("Đã hủy đơn đặt bàn #{} do khách hàng {} không còn lịch lưu trú hợp lệ.", reservation.getId(), customer.getFullName());
                
                // Send email
                try {
                    String subject = "Thông báo hủy đơn đặt bàn #" + reservation.getId();
                    String body = "Kính chào quý khách " + customer.getFullName() + ",\n\n" +
                                  "Hệ thống nhận thấy quý khách không có lịch lưu trú tại Resort vào ngày " + reserveDate + ".\n" +
                                  "Theo quy định, nhà hàng chỉ phục vụ trực tuyến cho khách đang lưu trú, do đó đơn đặt bàn #" + reservation.getId() + " của quý khách đã tự động bị hủy.\n\n" +
                                  "Xin lỗi vì sự bất tiện này. Chúc quý khách một ngày tốt lành!\n\n" +
                                  "Trân trọng,\nKawai Resort.";
                    emailService.sendEmail(customer.getEmail(), subject, body);
                } catch (Exception e) {
                    logger.error("Lỗi khi gửi email hủy đặt bàn: {}", e.getMessage());
                }
            }
        }
        
        logger.info("Hoàn tất quét dọn. Đã hủy {} đơn đặt bàn.", canceledCount);
    }
}
