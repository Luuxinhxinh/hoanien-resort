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
        private final PromotionRepository promotionRepository;
        private final BookingRepository bookingRepository;

        @Autowired(required = false)
        private EmailService emailService;

        public TourBookingServiceImpl(TourScheduleRepository tourScheduleRepository,
                        TourBookingRepository tourBookingRepository,
                        TourAttendeeRepository tourAttendeeRepository,
                        CustomerRepository customerRepository,
                        FolioItemRepository folioItemRepository,
                        TourStaffAssignmentRepository tourStaffAssignmentRepository,
                        EmployeeRepository employeeRepository,
                        RoomBookingDetailRepository roomBookingDetailRepository,
                        PromotionRepository promotionRepository,
                        BookingRepository bookingRepository) {
                this.tourScheduleRepository = tourScheduleRepository;
                this.tourBookingRepository = tourBookingRepository;
                this.tourAttendeeRepository = tourAttendeeRepository;
                this.customerRepository = customerRepository;
                this.folioItemRepository = folioItemRepository;
                this.tourStaffAssignmentRepository = tourStaffAssignmentRepository;
                this.employeeRepository = employeeRepository;
                this.roomBookingDetailRepository = roomBookingDetailRepository;
                this.promotionRepository = promotionRepository;
                this.bookingRepository = bookingRepository;
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

                // 3. Tính tổng giá (Dưới 2 tuổi miễn phí, 2 - 11 tuổi giảm 50%)
                BigDecimal totalPrice = BigDecimal.ZERO;
                BigDecimal basePrice = schedule.getTour().getBasePrice();
                int childCount = request.getChildAges() != null ? request.getChildAges().size() : 0;
                int adultCount = request.getParticipantCount() - childCount;
                if (adultCount < 0)
                        adultCount = 0;

                BigDecimal childDiscount = BigDecimal.ZERO;
                // Người lớn tính 100% giá
                totalPrice = totalPrice.add(basePrice.multiply(BigDecimal.valueOf(adultCount)));

                // Trẻ em tính theo độ tuổi
                if (request.getChildAges() != null) {
                        for (String age : request.getChildAges()) {
                                if ("Dưới 2 tuổi".equalsIgnoreCase(age)) {
                                        // Miễn phí
                                        childDiscount = childDiscount.add(basePrice);
                                } else if ("2 - 11 tuổi".equalsIgnoreCase(age)) {
                                        // Giảm 50%
                                        totalPrice = totalPrice.add(basePrice.multiply(new BigDecimal("0.5")));
                                        childDiscount = childDiscount.add(basePrice.multiply(new BigDecimal("0.5")));
                                } else {
                                        // Mặc định giảm 50%
                                        totalPrice = totalPrice.add(basePrice.multiply(new BigDecimal("0.5")));
                                        childDiscount = childDiscount.add(basePrice.multiply(new BigDecimal("0.5")));
                                }
                        }
                }

                // 4. Áp dụng mã giảm giá (nếu có)
                BigDecimal promoDiscount = BigDecimal.ZERO;
                Promotion appliedPromotion = null;
                if (request.getPromoCode() != null && !request.getPromoCode().trim().isEmpty()) {
                        String promoCode = request.getPromoCode().trim().toUpperCase();
                        java.util.Optional<Promotion> optPromo = promotionRepository.findByPromoCode(promoCode);
                        if (optPromo.isPresent()) {
                                Promotion promo = optPromo.get();
                                boolean isActive = Boolean.TRUE.equals(promo.getIsActive());
                                boolean notExpired = promo.getValidTo() == null
                                                || !promo.getValidTo().isBefore(LocalDate.now());
                                if (isActive && notExpired) {
                                        long uses = bookingRepository.countByCustomerIdAndPromoCode(customer.getId(), promoCode);
                                        if (uses >= 1) {
                                                throw new IllegalArgumentException("Khách hàng đã vượt quá số lần sử dụng mã giảm giá này (1 lần) [ERR_PROMO_USAGE_EXCEEDED]");
                                        }
                                        BigDecimal discountValue = promo.getDiscountValue();
                                        boolean isFixed = "FIXED_AMOUNT".equalsIgnoreCase(promo.getDiscountType())
                                                        || discountValue.compareTo(new BigDecimal("100")) >= 0;
                                        BigDecimal discountAmount;
                                        if (isFixed) {
                                                discountAmount = discountValue;
                                        } else {
                                                discountAmount = totalPrice.multiply(discountValue)
                                                                .divide(new BigDecimal("100"), 0,
                                                                                java.math.RoundingMode.HALF_UP);
                                        }
                                        if (discountAmount.compareTo(totalPrice) > 0) discountAmount = totalPrice;
                                        totalPrice = totalPrice.subtract(discountAmount);
                                        promoDiscount = discountAmount;
                                        appliedPromotion = promo;
                                        LOG.info("Áp dụng mã giảm giá '{}' cho tour booking: giảm {} VND",
                                                         promoCode, discountAmount);
                                } else {
                                        LOG.warn("Mã giảm giá '{}' không hợp lệ hoặc đã hết hạn", promoCode);
                                }
                        } else {
                                LOG.warn("Mã giảm giá '{}' không tồn tại trong hệ thống", promoCode);
                        }
                }

                // 5. Tạo TourBooking
                TourBooking booking = new TourBooking();
                booking.setSchedule(schedule);
                booking.setCustomer(customer);

                booking.setBookingDate(LocalDate.now());
                booking.setParticipantCount(request.getParticipantCount());
                booking.setBookingStatus("Confirmed");
                booking.setBookingSource("Direct_Web");
                booking.setTotalPrice(totalPrice);
                booking.setTourCharge(totalPrice);
                booking.setIsWalkInTour(request.isWalkInTour());
                if (appliedPromotion != null) {
                        booking.setAppliedPromotion(appliedPromotion);
                }

                // Lưu thông tin chi tiết vào notes để email hiển thị
                BigDecimal originalPrice = basePrice.multiply(new BigDecimal(request.getParticipantCount()));
                String pm = request.getPaymentMethod();
                String paymentMethodStr = pm;
                String paymentTypeStr = "full";

                if ("vnpay".equalsIgnoreCase(pm)) {
                        paymentMethodStr = "vnpay";
                        paymentTypeStr = request.getVnpPaymentType() != null ? request.getVnpPaymentType() : "deposit";
                } else if ("post-room".equalsIgnoreCase(pm) || request.isPostToRoom()) {
                        paymentMethodStr = "post-room";
                        paymentTypeStr = "room";
                } else if ("deposit".equalsIgnoreCase(pm)) {
                        paymentMethodStr = "counter";
                        paymentTypeStr = "deposit";
                } else if ("full".equalsIgnoreCase(pm)) {
                        paymentMethodStr = "counter";
                        paymentTypeStr = "full";
                }

                String customerNotes = request.getNotes() != null ? request.getNotes().trim() : "";
                customerNotes = customerNotes.replace(";", " ").replace("=", " ");
                String serializedNotes = "adults=" + adultCount
                                + ";children=" + childCount
                                + ";childDiscount=" + childDiscount
                                + ";promoDiscount=" + promoDiscount
                                + ";originalPrice=" + originalPrice
                                + ";paymentMethod=" + paymentMethodStr
                                + ";paymentType=" + paymentTypeStr
                                + ";customerNotes=" + customerNotes;
                booking.setNotes(serializedNotes);

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
                        BigDecimal limit = detail.getSubCreditLimit() != null ? detail.getSubCreditLimit()
                                        : BigDecimal.ZERO;
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
                // Nếu thanh toán qua VNPay, email sẽ được gửi sau khi VNPay xác nhận thành công
                // (trong VnPayServiceImpl.verifyIpn)
                if (emailService != null && !"vnpay".equalsIgnoreCase(request.getPaymentMethod())) {
                        String roomNumber = null;
                        if (request.isPostToRoom() && request.getRoomBookingDetailId() != null) {
                                RoomBookingDetail detail = roomBookingDetailRepository
                                                .findById(request.getRoomBookingDetailId()).orElse(null);
                                if (detail != null && detail.getRoom() != null) {
                                        roomNumber = detail.getRoom().getRoomNumber();
                                }
                        }
                        emailService.sendBookingConfirmation(savedBooking, customer, paymentMethodStr, paymentTypeStr,
                                        roomNumber);
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


        @jakarta.annotation.PostConstruct
        public void clearTourBookingsData() {
                try {
                        LOG.info("STARTING DATA CLEANUP FOR TOUR BOOKINGS AS REQUESTED...");
                        // 1. Delete tour attendees
                        tourAttendeeRepository.deleteAll();

                        // 2. Delete folio items that are linked to tour bookings
                        List<TourBooking> tbs = tourBookingRepository.findAll();
                        for (TourBooking tb : tbs) {
                                List<FolioItem> fis = folioItemRepository.findByBookingId(tb.getId());
                                if (fis != null && !fis.isEmpty()) {
                                        folioItemRepository.deleteAll(fis);
                                }
                        }

                        // 3. Delete tour bookings
                        tourBookingRepository.deleteAll();
                        LOG.info("TOUR BOOKINGS DATA CLEANUP COMPLETED SUCCESSFULLY.");
                } catch (Exception e) {
                        LOG.error("Failed to clean up tour bookings: " + e.getMessage());
                }
        }
}
