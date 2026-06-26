package com.kawai.services.impl;

import com.kawai.dto.TourBookingRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.EmailService;
import com.kawai.services.interfaces.TourBookingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của {@link TourBookingService} cho UC20.1: Đặt tour du lịch.
 *
 * <p>
 * Triển khai đầy đủ nghiệp vụ:
 * <ul>
 * <li>Validate schedule & customer tồn tại</li>
 * <li>Chống Double-booking: kiểm tra available slots trước khi tạo booking</li>
 * <li>Tạo TourBooking + N TourAttendee records</li>
 * <li>Hỗ trợ Post to Room: ghi nợ vào Folio phòng</li>
 * </ul>
 *
 * <p>
 * TDD Phase: 🟢 GREEN — Triển khai production code để pass tất cả test cases
 * (TC-M4-003, TC-M4-004, TC-M4-005).
 */
@Service
public class TourBookingServiceImpl implements TourBookingService {

        private static final Logger LOG = LoggerFactory.getLogger(TourBookingServiceImpl.class);

        private final TourScheduleRepository tourScheduleRepository;
        private final TourBookingRepository tourBookingRepository;
        private final TourAttendeeRepository tourAttendeeRepository;
        private final CustomerRepository customerRepository;
        private final FolioItemRepository folioItemRepository;
        private final TourStaffAssignmentRepository tourStaffAssignmentRepository;
        private final EmployeeRepository employeeRepository;
        private final RoomBookingDetailRepository roomBookingDetailRepository;

        @Autowired(required = false)
        private EmailService emailService;

        public TourBookingServiceImpl(TourScheduleRepository tourScheduleRepository,
                        TourBookingRepository tourBookingRepository,
                        TourAttendeeRepository tourAttendeeRepository,
                        CustomerRepository customerRepository,
                        FolioItemRepository folioItemRepository,
                        TourStaffAssignmentRepository tourStaffAssignmentRepository,
                        EmployeeRepository employeeRepository,
                        RoomBookingDetailRepository roomBookingDetailRepository) {
                this.tourScheduleRepository = tourScheduleRepository;
                this.tourBookingRepository = tourBookingRepository;
                this.tourAttendeeRepository = tourAttendeeRepository;
                this.customerRepository = customerRepository;
                this.folioItemRepository = folioItemRepository;
                this.tourStaffAssignmentRepository = tourStaffAssignmentRepository;
                this.employeeRepository = employeeRepository;
                this.roomBookingDetailRepository = roomBookingDetailRepository;
        }

        @Override
        public Long createTourBooking(TourBookingRequest request) {
                // 1. Validate schedule & customer
                TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                                .orElseThrow(() -> new IllegalStateException("TOUR-002: Schedule not found"));
                Customer customer = customerRepository.findById(request.getCustomerId())
                                .orElseThrow(() -> new IllegalStateException("TOUR-003: Customer not found"));

                // 2. Chống Double-booking: kiểm tra available slots
                int alreadyBooked = tourBookingRepository.countByScheduleAndBookingStatus(schedule, "Confirmed");
                int remainingCapacity = schedule.getTour().getMaxCapacity() - alreadyBooked;

                if (request.getParticipantCount() > remainingCapacity) {
                        LOG.warn("TOUR-001: Tour schedule {} hết chỗ. Already={}, Request={}, Max={}",
                                        schedule.getId(), alreadyBooked, request.getParticipantCount(),
                                        schedule.getTour().getMaxCapacity());
                        throw new IllegalStateException(
                                        "TOUR-001: Hết chỗ. Chỉ còn " + remainingCapacity + " chỗ trống");
                }

                // 3. Tính tổng giá
                BigDecimal totalPrice = schedule.getTour().getBasePrice()
                                .multiply(BigDecimal.valueOf(request.getParticipantCount()));

                // 4. Tạo TourBooking
                TourBooking booking = new TourBooking();
                booking.setSchedule(schedule);
                booking.setCustomer(customer);
                booking.setBookingDate(LocalDate.now());
                booking.setParticipantCount(request.getParticipantCount());
                booking.setIsWalkInTour(request.isWalkInTour());
                booking.setBookingStatus("Confirmed");
                booking.setBookingSource("Direct_Web");
                booking.setTotalPrice(totalPrice);
                booking.setTourCharge(totalPrice);

                TourBooking savedBooking = tourBookingRepository.save(booking);
                LOG.info("Created tour booking {} for schedule {} ({} pax)",
                                savedBooking.getId(), schedule.getId(), request.getParticipantCount());

                // 5. Tạo TourAttendee records
                List<TourAttendee> attendees = new ArrayList<>();
                for (int i = 0; i < request.getParticipantCount(); i++) {
                        TourAttendee attendee = new TourAttendee();
                        attendee.setTourBooking(savedBooking);
                        if (i == 0) {
                                attendee.setCustomer(customer);
                        }
                        attendee.setAttendanceStatus("Not_Show");
                        attendees.add(attendee);
                }
                tourAttendeeRepository.saveAll(attendees);

                // 6. Post to Room: ghi nợ vào Folio phòng
                if (request.isPostToRoom()) {
                        // BR-TR-08: Khi chọn Post to Room, bắt buộc phải cung cấp roomBookingDetailId
                        // hợp lệ (phòng đã check-in). Nếu không → TOUR-004.
                        if (request.getRoomBookingDetailId() == null) {
                                LOG.warn("TOUR-004: Booking {} yêu cầu Post to Room nhưng không cung cấp roomBookingDetailId",
                                                savedBooking.getId());
                                throw new IllegalStateException(
                                                "TOUR-004: Vui lòng chọn phòng để ghi nợ. Phòng phải đã được check-in.");
                        }

                        // BR-TR-09: roomBookingDetailId phải tồn tại trong hệ thống → TOUR-005
                        RoomBookingDetail detail = roomBookingDetailRepository
                                        .findById(request.getRoomBookingDetailId())
                                        .orElseThrow(() -> {
                                                LOG.warn("TOUR-005: RoomBookingDetail {} không tồn tại trong hệ thống",
                                                                request.getRoomBookingDetailId());
                                                return new IllegalStateException(
                                                                "TOUR-005: Chi tiết đặt phòng không tồn tại hoặc đã bị xóa. ID: "
                                                                                + request.getRoomBookingDetailId());
                                        });

                        // Check Folio Credit Limit
                        BigDecimal limit = detail.getSubCreditLimit() != null ? detail.getSubCreditLimit() : BigDecimal.ZERO;
                        BigDecimal used = folioItemRepository.findByRoomBookingDetailId(detail.getId()).stream()
                                .map(FolioItem::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        if (limit.subtract(used).compareTo(totalPrice) < 0) {
                                throw new IllegalStateException(
                                                "TOUR-LIMIT: Hạn mức chi tiêu của phòng không đủ để thanh toán tour. Vui lòng thanh toán bớt nợ cũ hoặc chọn hình thức TT Trực Tuyến.");
                        }

                        FolioItem folioItem = new FolioItem();
                        folioItem.setBooking(savedBooking);
                        folioItem.setRoomBookingDetail(detail);
                        folioItem.setPayerCustomer(customer);
                        folioItem.setSourceDepartment("Tour");
                        folioItem.setAmount(totalPrice);
                        folioItem.setDescription("Tour: " + schedule.getTour().getTourName()
                                        + " (" + request.getParticipantCount() + " pax)");
                        folioItem.setIsSettledSeparately(false);

                        folioItemRepository.save(folioItem);
                        LOG.info("Post to Room: FolioItem tạo thành công cho tour booking {} — {} VND, RoomBookingDetail {}",
                                        savedBooking.getId(), totalPrice, detail.getId());
                } else {
                        // Không Post to Room — bỏ qua bước ghi Folio hoàn toàn
                        LOG.debug("Booking {}: postToRoom=false, bỏ qua ghi Folio", savedBooking.getId());
                }

                // 7. Gửi email xác nhận đặt tour (bất đồng bộ, không block)
                if (emailService != null) {
                        String roomNumber = null;
                        if (request.isPostToRoom() && request.getRoomBookingDetailId() != null) {
                                RoomBookingDetail detail = roomBookingDetailRepository.findById(request.getRoomBookingDetailId()).orElse(null);
                                if (detail != null && detail.getRoom() != null) {
                                        roomNumber = detail.getRoom().getRoomNumber();
                                }
                        }
                        emailService.sendBookingConfirmation(savedBooking, customer, request.isPostToRoom(), roomNumber);
                }

                return savedBooking.getId();
        }

        @Override
        public void scheduleTour(Long scheduleId, Long employeeId, String staffRole) {
                // UC20.2: Lập lịch chuyến tour — gán nhân viên (Tour Guide / Tài xế) vào lịch
                // trình
                // Business Rule: BR-TR-06 — Cảnh báo Admin nếu chưa đủ Minimum Pax trước 24h,
                // nhưng logic gán nhân viên vẫn được thực hiện độc lập ở đây.
                TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                                .orElseThrow(() -> new IllegalStateException("TOUR-002: Schedule not found"));
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new IllegalStateException("TOUR-003: Employee not found"));

                TourStaffAssignment assignment = new TourStaffAssignment();
                assignment.setSchedule(schedule);
                assignment.setEmployee(employee);
                assignment.setStaffRole(staffRole);

                tourStaffAssignmentRepository.save(assignment);
                LOG.info("Assigned employee {} ({}) to schedule {}", employeeId, staffRole, scheduleId);
        }

        @Override
        public BigDecimal cancelTour(Long bookingId, boolean cancelledByResort) {
                // UC20.3: Hủy tour lữ hành và tính toán tiền hoàn cọc
                // BR-TR-05: Hủy do Resort → hoàn 100%; Khách tự hủy trong 24h → mất 50%
                TourBooking booking = tourBookingRepository.findById(bookingId)
                                .orElseThrow(() -> new IllegalStateException("TOUR-002: Booking not found"));

                BigDecimal refundAmount;
                String newStatus;

                if (cancelledByResort) {
                        // Hủy do phía Resort: hoàn tiền 100%
                        refundAmount = booking.getTotalPrice();
                        newStatus = "Cancelled_Refunded";
                } else {
                        // Khách tự hủy (trong vòng 24h trước giờ tour): mất 50% cọc
                        refundAmount = booking.getTotalPrice().multiply(new BigDecimal("0.5"));
                        newStatus = "Cancelled_Forfeited";
                }

                booking.setBookingStatus(newStatus);
                tourBookingRepository.save(booking);

                LOG.info("Cancelled booking {} (resort={}), refund={}, status={}",
                                bookingId, cancelledByResort, refundAmount, newStatus);

                // Gửi email thông báo hủy tour (bất đồng bộ)
                if (emailService != null && booking.getCustomer() != null) {
                        emailService.sendCancellationNotice(
                                booking, booking.getCustomer(), refundAmount, cancelledByResort);
                }

                return refundAmount;
        }
}
