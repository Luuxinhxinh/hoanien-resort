package com.hoanien.services.impl;

import com.hoanien.dto.TourBookingRequest;
import com.hoanien.models.*;
import com.hoanien.repositories.*;
import com.hoanien.services.interfaces.EmailService;
import com.hoanien.services.interfaces.TourBookingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        @Autowired(required = false)
        private com.hoanien.services.interfaces.SystemNotificationService systemNotificationService;

        @Autowired
        private com.hoanien.repositories.RefundRequestRepository refundRequestRepository;

        @Autowired(required = false)
        private com.hoanien.services.interfaces.FolioService folioService;

        @Autowired
        private DependentRepository dependentRepository;

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

                // Kiểm tra ngày khởi hành: chỉ cho phép đặt tour khởi hành từ ngày mai trở đi
                if (schedule.getDepartureDate() != null) {
                        LocalDate departureDate = schedule.getDepartureDate();
                        if (!departureDate.isAfter(LocalDate.now())) {
                                throw new IllegalStateException(
                                                "TOUR-DATE-001: Chỉ được đặt các chuyến tour khởi hành từ ngày mai trở đi (phải đặt trước ít nhất 1 ngày).");
                        }
                }

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

                Tour tour = schedule.getTour();
                if (Boolean.TRUE.equals(tour.getIsInsuranceRequired()) && !request.isAcceptInsurance()) {
                        LOG.warn("TOUR-INS-001: Khách {} từ chối bảo hiểm bắt buộc cho tour {}",
                                        request.getCustomerId(), tour.getId());
                        throw new IllegalStateException(
                                        "TOUR-INS-001: Tour này bắt buộc mua bảo hiểm du lịch. Vui lòng đồng ý mua bảo hiểm để tiếp tục đặt chỗ.");
                }
                BigDecimal totalPrice = BigDecimal.ZERO;
                BigDecimal basePrice = tour.getBasePrice();
                int childCount = request.getChildAges() != null ? request.getChildAges().size() : 0;
                int adultCount = request.getParticipantCount() - childCount;
                if (adultCount < 0)
                        adultCount = 0;

                BigDecimal childDiscount = BigDecimal.ZERO;
                totalPrice = totalPrice.add(basePrice.multiply(BigDecimal.valueOf(adultCount)));

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

                // 3b. Tính phí bảo hiểm để hạch toán (đã bao gồm trong giá
                // tour gốc)
                BigDecimal insuranceFee = BigDecimal.ZERO;
                if (Boolean.TRUE.equals(tour.getIsInsuranceRequired()) && request.isAcceptInsurance()) {
                        insuranceFee = tour.getInsurancePrice()
                                        .multiply(BigDecimal.valueOf(request.getParticipantCount()));
                        LOG.info("Phí bảo hiểm hạch toán (đã bao gồm trong giá): {} x {} người = {} VND",
                                        tour.getInsurancePrice(), request.getParticipantCount(), insuranceFee);
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
                                        long uses = bookingRepository.countByCustomerIdAndPromoCode(customer.getId(),
                                                        promoCode);
                                        if (uses >= 1) {
                                                throw new IllegalArgumentException(
                                                                "Khách hàng đã vượt quá số lần sử dụng mã giảm giá này (1 lần) [ERR_PROMO_USAGE_EXCEEDED]");
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
                                        if (discountAmount.compareTo(totalPrice) > 0)
                                                discountAmount = totalPrice;
                                        totalPrice = totalPrice.subtract(discountAmount);
                                        promoDiscount = discountAmount;
                                        appliedPromotion = promo;
                                        LOG.info("Áp dụng mã giảm giá '{}' cho tour booking: giảm {} VND",
                                                        promoCode, discountAmount);
                                } else {
                                        LOG.warn("Mã giảm giá '{}' không hợp lệ hoặc đã hết hạn",
                                                        promoCode);
                                }
                        } else {
                                LOG.warn("Mã giảm giá '{}' không tồn tại trong hệ thống", promoCode);
                        }
                }

                // Pre-validate Post to Room Credit Limit before creating TourBooking entity
                RoomBookingDetail postRoomDetail = null;
                if (request.isPostToRoom()) {
                        if (request.getRoomBookingDetailId() == null) {
                                LOG.warn("TOUR-004: Booking yêu cầu Post to Room nhưng không cung cấp roomBookingDetailId");
                                throw new IllegalStateException(
                                                "TOUR-004: Vui lòng chọn phòng để ghi nợ. Phòng phải đã được check-in.");
                        }

                        postRoomDetail = roomBookingDetailRepository
                                        .findById(request.getRoomBookingDetailId())
                                        .orElseThrow(() -> {
                                                LOG.warn("TOUR-005: RoomBookingDetail {} không tồn tại trong hệ thống",
                                                                request.getRoomBookingDetailId());
                                                return new IllegalStateException(
                                                                "TOUR-005: Chi tiết đặt phòng không tồn tại hoặc đã bị xóa. ID: "
                                                                                + request.getRoomBookingDetailId());
                                        });

                        // Check Folio Credit Limit
                        BigDecimal subLimit = postRoomDetail.getSubCreditLimit();
                        BigDecimal limit = subLimit != null ? subLimit : BigDecimal.ZERO;
                        java.util.List<FolioItem> folioItems = folioItemRepository
                                        .findByRoomBookingDetailId(postRoomDetail.getId());
                        // Đã nạp thêm hạn mức (không tính tiền cọc walk-in)
                        BigDecimal creditTopUp = folioItems.stream()
                                        .filter(fi -> !Boolean.TRUE.equals(fi.getIsSettledSeparately()))
                                        .filter(fi -> fi.getDescription() != null && fi.getDescription().startsWith("Nạp tiền nâng hạn mức"))
                                        .map(FolioItem::getAmount)
                                        .filter(a -> a != null && a.compareTo(BigDecimal.ZERO) < 0)
                                        .map(BigDecimal::abs)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        // Tổng chi tiêu thực tế (Dương = Phí dịch vụ, Âm = Hoàn tiền/Giảm trừ, LOẠI BỎ các khoản nạp hạn mức)
                        BigDecimal netCharged = folioItems.stream()
                                        .filter(fi -> !Boolean.TRUE.equals(fi.getIsSettledSeparately()))
                                        .filter(fi -> fi.getDescription() == null || !fi.getDescription().startsWith("Nạp tiền nâng hạn mức"))
                                        .map(FolioItem::getAmount)
                                        .filter(a -> a != null)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        if (netCharged.compareTo(BigDecimal.ZERO) < 0) {
                                netCharged = BigDecimal.ZERO;
                        }
                        BigDecimal available = limit.add(creditTopUp).subtract(netCharged);

                        if (available.compareTo(totalPrice) < 0) {
                                LOG.warn("TOUR-LIMIT BLOCKED: Hạn mức chi tiêu của phòng {} không đủ để thanh toán tour (Available: {}, Price: {})",
                                                postRoomDetail.getId(), available, totalPrice);
                                throw new IllegalStateException(
                                                "Hạn mức ghi nợ của phòng bị quá hạn, vui lòng tới quầy lễ tân để làm thủ tục nâng lên hạn mức hoặc thanh toán chuyển khoản trực tiếp cho đơn này.");
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

                if (request.getRoomBookingId() != null) {
                        booking.setRoomBooking((RoomBooking) bookingRepository.findById(request.getRoomBookingId())
                                        .orElse(null));
                }
                if (request.getRoomBookingDetailId() != null) {
                        booking.setRoomBookingDetail(roomBookingDetailRepository
                                        .findById(request.getRoomBookingDetailId()).orElse(null));
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
                                + ";insuranceFee=" + insuranceFee
                                + ";paymentMethod=" + paymentMethodStr
                                + ";paymentType=" + paymentTypeStr
                                + ";customerNotes=" + customerNotes;
                booking.setNotes(serializedNotes);

                TourBooking savedBooking = tourBookingRepository.save(booking);
                LOG.info("Created tour booking {} for schedule {} ({} pax)",
                                savedBooking.getId(), schedule.getId(), request.getParticipantCount());

                // Update bookedSeats on TourSchedule
                int currentBooked = schedule.getBookedSeats() != null ? schedule.getBookedSeats() : 0;
                schedule.setBookedSeats(currentBooked + request.getParticipantCount());
                tourScheduleRepository.save(schedule);

                // 5a. Sinh mã bảo hiểm và đánh dấu schedule (chỉ khi tour bắt
                // buộc bảo hiểm)
                if (Boolean.TRUE.equals(tour.getIsInsuranceRequired()) && request.isAcceptInsurance()) {
                        if (!Boolean.TRUE.equals(schedule.getIsInsuranceProcessed())) {
                                // Chưa có mã → sinh mới
                                String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                                String policyNumber = "INS-" + LocalDate.now()
                                                + "-SCH" + schedule.getId()
                                                + "-" + suffix;
                                schedule.setInsurancePolicyNumber(policyNumber);
                                schedule.setIsInsuranceProcessed(true);
                                tourScheduleRepository.save(schedule);
                                LOG.info("Sinh mã bảo hiểm cho schedule {}: {}", schedule.getId(), policyNumber);
                        } else {
                                LOG.info("Schedule {} đã có mã bảo hiểm: {}", schedule.getId(),
                                                schedule.getInsurancePolicyNumber());
                        }
                }

                // 5. Tạo TourAttendee records
                List<TourAttendee> attendees = new ArrayList<>();
                int currentAttendeeCount = 0;

                // Kiểm tra xem khách hàng đặt chính đã đăng ký tham gia chuyến đi này trước đó
                // chưa
                boolean isCustomerAlreadyRegistered = tourAttendeeRepository
                                .existsActiveAttendeeByCustomerAndSchedule(customer.getId(), schedule.getId());

                // Khách hàng đặt chính là attendee số 1 (Chỉ gán nếu khách hàng chọn tham gia chuyến đi và chưa đăng ký tham gia chuyến đi này)
                boolean shouldAddMainCustomer = request.isPayerParticipating() && !isCustomerAlreadyRegistered;
                if (shouldAddMainCustomer && currentAttendeeCount < request.getParticipantCount()) {
                        TourAttendee mainAttendee = new TourAttendee();
                        mainAttendee.setTourBooking(savedBooking);
                        mainAttendee.setCustomer(customer);
                        mainAttendee.setAttendanceStatus("Not_Show");
                        mainAttendee.setFaceVectorData(customer.getFaceVectorData());
                        attendees.add(mainAttendee);
                        currentAttendeeCount++;
                }

                // Táº¡o cÃ¡c attendee Ä‘i kÃ¨m dá»±a trÃªn danh sÃ¡ch companions (ngÆ°á»i
                // lớn đi cùng)
                if (request.getCompanions() != null && !request.getCompanions().isEmpty()) {
                        for (TourBookingRequest.CompanionRequest comp : request.getCompanions()) {
                                if (currentAttendeeCount >= request.getParticipantCount()) {
                                        break;
                                }

                                Dependent savedDep = null;
                                if (comp.getDependentId() != null && comp.getDependentId() > 0) {
                                    savedDep = dependentRepository.findById(comp.getDependentId()).orElse(null);
                                }
                                
                                if (savedDep == null) {
                                    Dependent dep = new Dependent();
                                    dep.setCustomer(customer);
                                    dep.setDependentName(comp.getName());
                                    int age = comp.getAge() != null ? comp.getAge() : 12;
                                    dep.setBirthDate(LocalDate.now().minusYears(age));
                                    dep.setGender("Nam");
                                    dep.setIsDeleted(false);

                                    if (comp.getIdCard() != null && !comp.getIdCard().trim().isEmpty()) {
                                            dep.setCccdPassportEncrypted(com.hoanien.utils.EncryptionUtils
                                                            .encrypt(comp.getIdCard().trim()));
                                    } else if (comp.getPhone() != null && !comp.getPhone().trim().isEmpty()) {
                                            dep.setCccdPassportEncrypted("PHONE_" + comp.getPhone().trim());
                                    }
                                    savedDep = dependentRepository.save(dep);
                                }

                                TourAttendee attendee = new TourAttendee();
                                attendee.setTourBooking(savedBooking);
                                attendee.setDependent(savedDep);
                                attendee.setAttendanceStatus("Not_Show");
                                if (savedDep != null) {
                                        attendee.setFaceVectorData(savedDep.getFaceVectorData());
                                }
                                attendees.add(attendee);
                                currentAttendeeCount++;
                        }
                }

                // Nếu còn thừa slot (trẻ em chưa nhập chi tiết companion),
                // Tạo các attendee trẻ em từ danh sách childAges thực tế được
                // gửi lên
                java.util.List<String> childAges = request.getChildAges() != null
                                ? new java.util.ArrayList<>(request.getChildAges())
                                : new java.util.ArrayList<>();
                for (String rawAge : childAges) {
                        if (currentAttendeeCount >= request.getParticipantCount()) {
                                break;
                        }

                        String name = "";
                        String ageLabel = "";
                        if (rawAge.contains("|")) {
                                String[] parts = rawAge.split("\\|");
                                name = parts[0].trim();
                                ageLabel = parts[1].trim();
                        } else {
                                name = "Trẻ em (" + rawAge + ")";
                                ageLabel = rawAge;
                        }

                        // Tính năm sinh ước lượng từ nhãn tuổi
                        int estimatedAge = 12; // default: adult
                        if ("Dưới 2 tuổi".equalsIgnoreCase(ageLabel)) {
                                estimatedAge = 1;
                        } else if ("2 - 11 tuổi".equalsIgnoreCase(ageLabel)) {
                                estimatedAge = 6;
                        }

                        Dependent childDep = new Dependent();
                        childDep.setCustomer(customer);
                        childDep.setDependentName(name);
                        childDep.setBirthDate(LocalDate.now().minusYears(estimatedAge));
                        childDep.setGender("Không xác định");
                        childDep.setIsDeleted(false);
                        
                        Dependent savedChildDep = dependentRepository.save(childDep);
                        TourAttendee attendee = new TourAttendee();
                        attendee.setTourBooking(savedBooking);
                        attendee.setDependent(savedChildDep);
                        attendee.setAttendanceStatus("Not_Show");
                        attendees.add(attendee);
                        currentAttendeeCount++;
                }
                
                tourAttendeeRepository.saveAll(attendees);

                // 6. Post to Room: ghi nợ vào Folio phòng
                if (request.isPostToRoom()) {
                        RoomBookingDetail detail = postRoomDetail != null ? postRoomDetail
                                        : roomBookingDetailRepository.findById(request.getRoomBookingDetailId()).orElse(null);

                        if (detail != null) {
                                FolioItem folioItem = new FolioItem();
                                folioItem.setBooking(savedBooking);
                                folioItem.setRoomBookingDetail(detail);
                                folioItem.setPayerCustomer(customer);
                                folioItem.setSourceDepartment("Tour");
                                folioItem.setAmount(totalPrice);
                                folioItem.setDescription("Tour: " + schedule.getTour().getTourName()
                                                + " (" + request.getParticipantCount() + " pax)");
                                folioItem.setIsSettledSeparately(false);
                                folioItem.setRevenueCode("OTH_TOUR");
                                folioItemRepository.save(folioItem);
                                LOG.info("Post to Room: FolioItem tạo thành công cho tour booking {} — {} VND, RoomBookingDetail {}",
                                                savedBooking.getId(), totalPrice, detail.getId());
                        }
                } else {
                        // KhÃ´ng Post to Room â€” bá» qua bÆ°á»›c ghi Folio hoÃ n toÃ n
                        LOG.debug("Booking {}: postToRoom=false, bỏ qua ghi Folio", savedBooking.getId());
                }

                // 7. Gửi email xác nhận đặt tour (bất đồng bộ, không block)
                // Nếu thanh toán qua VNPay, email sẽ được gửi sau khi VNPay xác
                // nhận thành công
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

                // Tự động gán Tour Guide cho schedule của booking này theo đúng phân công tour / lịch làm việc
                try {
                        java.time.LocalDate depDate = schedule.getDepartureDate();
                        java.time.LocalTime depTime = schedule.getDepartureTime();

                        // ID các Tour Guides phụ trách theo tour:
                        // 5L = Nguyễn Ngọc (Huế - Đoàn tụ)
                        // 41L = Đỗ Thu Bình (Quảng Nam - Đồng nội)
                        // 42L = Ngô Minh Lan (Ninh Bình - Di sản thủ công)
                        // 47L = Phan Thanh Hải (Tháp Mười - Liên hoa)
                        // 48L = Nguyễn Quỳnh Anh (Hạ Long - Sóng xanh)
                        // 49L = Lê Hoàng Long (Sapa - Sương phủ)
                        // 50L = Vũ Thu Trang (Cát Tiên - Nhịp đập)
                        Long preferredGuideId = 5L;
                        if (schedule.getTour() != null) {
                                String tn = schedule.getTour().getTourName() != null ? schedule.getTour().getTourName().toLowerCase() : "";
                                Long tId = schedule.getTour().getId();
                                if ((tId != null && tId == 1L) || tn.contains("đoàn tụ") || tn.contains("doantu")) {
                                        preferredGuideId = 5L;
                                } else if ((tId != null && tId == 2L) || tn.contains("đồng nội") || tn.contains("dongnoi")) {
                                        preferredGuideId = 41L;
                                } else if ((tId != null && tId == 3L) || tn.contains("di sản") || tn.contains("thủ công") || tn.contains("disan")) {
                                        preferredGuideId = 42L;
                                } else if ((tId != null && tId == 4L) || tn.contains("liên hoa") || tn.contains("tháp mười") || tn.contains("tinhlang")) {
                                        preferredGuideId = 47L;
                                } else if ((tId != null && tId == 5L) || tn.contains("sóng xanh") || tn.contains("hạ long") || tn.contains("halong")) {
                                        preferredGuideId = 48L;
                                } else if ((tId != null && tId == 6L) || tn.contains("sương phủ") || tn.contains("sapa") || tn.contains("suongphu")) {
                                        preferredGuideId = 49L;
                                } else if ((tId != null && tId == 7L) || tn.contains("nhịp đập") || tn.contains("cát tiên") || tn.contains("cattien") || tn.contains("nhipdap")) {
                                        preferredGuideId = 50L;
                                }
                        }

                        Long selectedGuideId = preferredGuideId;

                        if (depDate != null && depTime != null) {
                                boolean prefConflict = false;
                                List<TourStaffAssignment> prefAssigns = tourStaffAssignmentRepository
                                                .findByEmployeeId(preferredGuideId);
                                if (prefAssigns != null) {
                                        for (TourStaffAssignment a : prefAssigns) {
                                                if (a.getSchedule() != null
                                                                && !a.getSchedule().getId().equals(schedule.getId())) {
                                                        if (depDate.equals(a.getSchedule().getDepartureDate()) &&
                                                                        depTime.equals(a.getSchedule().getDepartureTime()) &&
                                                                        "GUIDE".equalsIgnoreCase(a.getStaffRole())) {
                                                                prefConflict = true;
                                                                break;
                                                        }
                                                }
                                        }
                                }

                                if (prefConflict) {
                                        Long[] candidateGuideIds = new Long[]{5L, 41L, 42L, 47L, 48L, 49L, 50L};
                                        for (Long candId : candidateGuideIds) {
                                                if (candId.equals(preferredGuideId)) continue;
                                                boolean candConflict = false;
                                                List<TourStaffAssignment> candAssigns = tourStaffAssignmentRepository.findByEmployeeId(candId);
                                                if (candAssigns != null) {
                                                        for (TourStaffAssignment a : candAssigns) {
                                                                if (a.getSchedule() != null && !a.getSchedule().getId().equals(schedule.getId())) {
                                                                        if (depDate.equals(a.getSchedule().getDepartureDate()) &&
                                                                            depTime.equals(a.getSchedule().getDepartureTime()) &&
                                                                            "GUIDE".equalsIgnoreCase(a.getStaffRole())) {
                                                                                candConflict = true;
                                                                                break;
                                                                        }
                                                                }
                                                        }
                                                }
                                                if (!candConflict) {
                                                        selectedGuideId = candId;
                                                        break;
                                                }
                                        }
                                }
                        }

                        Employee guide = employeeRepository.findById(selectedGuideId).orElse(null);
                        if (guide != null) {
                                List<TourStaffAssignment> assignments = tourStaffAssignmentRepository
                                                .findByScheduleId(schedule.getId());
                                TourStaffAssignment guideAssignment = null;
                                if (assignments != null) {
                                        for (TourStaffAssignment a : assignments) {
                                                if ("GUIDE".equalsIgnoreCase(a.getStaffRole())) {
                                                        guideAssignment = a;
                                                        break;
                                                }
                                        }
                                }
                                if (guideAssignment == null) {
                                        guideAssignment = new TourStaffAssignment();
                                        guideAssignment.setSchedule(schedule);
                                        guideAssignment.setStaffRole("GUIDE");
                                }
                                guideAssignment.setEmployee(guide);
                                tourStaffAssignmentRepository.save(guideAssignment);
                                LOG.info("Đã gán Tour Guide {} (ID {}) cho schedule ID: {}", guide.getFullName(),
                                                selectedGuideId, schedule.getId());
                        }
                } catch (Exception e) {
                        LOG.error("Lỗi khi tự động gán Tour Guide theo luật thời gian: {}", e.getMessage());
                }

                return savedBooking.getId();
        }

        @Override
        public void scheduleTour(Long scheduleId, Long employeeId, String staffRole) {
                // UC20.2: Lập lịch chuyến tour — gán nhân viên (Tour Guide / Tài
                // xế) vào lịch
                // trình
                // Business Rule: BR-TR-06 — Cảnh báo Admin nếu chưa đủ Minimum Pax
                // trước 24h,
                // nhưng logic gán nhân viên vẫn được thực hiện độc lập ở
                // đây.
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
        public BigDecimal cancelTour(Long bookingId, boolean cancelledByResort, String reason) {
                // UC20.3: Hủy tour lữ hành và tính toán tiền hoàn cọc
                // BR-TR-05: Hủy do Resort → hoàn 100%; Khách tự hủy trong 24h →
                // mất 50%
                TourBooking booking = tourBookingRepository.findById(bookingId)
                                .orElseThrow(() -> new IllegalStateException("TOUR-002: Booking not found"));

                BigDecimal refundAmount;
                String newStatus;

                if (cancelledByResort) {
                        // Hủy do phía Resort: hoàn tiền 100%
                        refundAmount = booking.getTotalPrice();
                        newStatus = "Cancelled_Refunded";
                        booking.setBookingStatus(newStatus);
                        tourBookingRepository.save(booking);

                        // A. Nếu là khách lưu trú (có phòng nghỉ): Xóa FolioItem của Tour ra khỏi Folio phòng do hoàn 100%
                        if (booking.getRoomBookingDetail() != null) {
                                Long detailId = booking.getRoomBookingDetail().getId();
                                if (folioItemRepository != null) {
                                        List<FolioItem> items = folioItemRepository.findByRoomBookingDetailId(detailId);
                                        if (items != null) {
                                                String tourName = (booking.getSchedule() != null && booking.getSchedule().getTour() != null)
                                                                ? booking.getSchedule().getTour().getTourName() : "";
                                                for (FolioItem item : items) {
                                                        boolean isTourItem = "Tour".equalsIgnoreCase(item.getSourceDepartment())
                                                                        || "OTH_TOUR".equalsIgnoreCase(item.getRevenueCode())
                                                                        || (item.getDescription() != null && item.getDescription().contains("Tour:"));
                                                        if (isTourItem) {
                                                                if (tourName.isEmpty() || (item.getDescription() != null && item.getDescription().contains(tourName))) {
                                                                        folioItemRepository.delete(item);
                                                                }
                                                        }
                                                }
                                        }
                                }

                                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                                String formattedTime = java.time.LocalDateTime.now().withNano(0).format(dtf);
                                String formattedAmount = String.format("%,d", refundAmount.longValue()).replace(',', '.');
                                String roomNum = (booking.getRoomBookingDetail().getRoom() != null ? booking.getRoomBookingDetail().getRoom().getRoomNumber() : "");

                                String currentNotes = booking.getNotes() != null ? booking.getNotes().trim() : "";
                                String noteEntry = "[HỦY TOUR & HOÀN 100%] Xóa khoản Tour (" + formattedAmount + " VNĐ) khỏi Folio P." + roomNum + " (" + formattedTime + "). Lý do: " + reason;
                                if (currentNotes.isEmpty()) {
                                    booking.setNotes(noteEntry);
                                } else {
                                    booking.setNotes(currentNotes + "\n" + noteEntry);
                                }
                                tourBookingRepository.save(booking);
                        }
                        // B. Nếu là khách vãng lai (không có phòng): Tạo RefundRequest chờ chuyển khoản
                        else {
                                com.hoanien.models.RefundRequest refund = new com.hoanien.models.RefundRequest();
                                refund.setTourBooking(booking);
                                if (booking.getCustomer() != null) {
                                    refund.setAccountName(booking.getCustomer().getFullName());
                                    refund.setPhoneNumber(booking.getCustomer().getPhone());
                                }
                                refund.setAmount(refundAmount);
                                refund.setStatus("Pending");
                                refund.setManagerNote("Hoàn 100% tiền Tour do Resort hủy tour. Sự cố: " + reason);
                                refund.setCreatedAt(java.time.LocalDateTime.now().withNano(0));
                                refundRequestRepository.save(refund);

                                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                                String formattedTime = java.time.LocalDateTime.now().withNano(0).format(dtf);
                                String formattedAmount = String.format("%,d", refundAmount.longValue()).replace(',', '.');

                                String currentNotes = booking.getNotes() != null ? booking.getNotes().trim() : "";
                                String noteEntry = "[TẠO YÊU CẦU HOÀN] Hoàn 100% tiền Tour (" + formattedAmount + " VNĐ) - Chờ chuyển khoản (" + formattedTime + "). Lý do: " + reason;
                                if (currentNotes.isEmpty()) {
                                    booking.setNotes(noteEntry);
                                } else {
                                    booking.setNotes(currentNotes + "\n" + noteEntry);
                                }
                                tourBookingRepository.save(booking);
                        }
                } else {
                        // Khách tự hủy (trong vòng 24h trước giờ tour): mất 50% cọc
                        refundAmount = booking.getTotalPrice().multiply(new BigDecimal("0.5"));
                        newStatus = "Cancelled_Forfeited";
                        booking.setBookingStatus(newStatus);
                        tourBookingRepository.save(booking);
                }

                // Cập nhật số ghế của TourSchedule
                TourSchedule schedule = booking.getSchedule();
                if (schedule != null && booking.getParticipantCount() != null) {
                        int newSeats = schedule.getBookedSeats() - booking.getParticipantCount();
                        if (newSeats < 0)
                                newSeats = 0;
                        schedule.setBookedSeats(newSeats);

                        if (newSeats == 0) {
                                schedule.setScheduleStatus("Cancelled");
                                LOG.info("TourSchedule {} bị hủy vì toàn bộ khách đã hủy (số ghế = 0)",
                                                schedule.getId());

                                // Xóa các phân công nhân viên và thông báo
                                List<TourStaffAssignment> assignments = tourStaffAssignmentRepository
                                                .findByScheduleId(schedule.getId());
                                if (assignments != null && !assignments.isEmpty()) {
                                        for (TourStaffAssignment a : assignments) {
                                                Employee emp = a.getEmployee();
                                                if ("GUIDE".equalsIgnoreCase(a.getStaffRole()) && emp != null) {
                                                        LOG.info("Giải phóng Tour Guide {} khỏi TourSchedule {}",
                                                                        emp.getFullName(), schedule.getId());
                                                        tourStaffAssignmentRepository.delete(a);

                                                        // Thông báo cho nhân viên qua Email (sử dụng HTML log
                                                        // hoặc email service)
                                                        if (emailService != null && emp.getEmail() != null) {
                                                                String content = "<h2>Thông báo Hủy Lịch Trình</h2>"
                                                                                + "<p>Xin chào " + emp.getFullName()
                                                                                + ",</p>"
                                                                                + "<p>Lịch trình tour <b>"
                                                                                + (schedule.getTour() != null ? schedule
                                                                                                .getTour().getTourName()
                                                                                                : "")
                                                                                + "</b> "
                                                                                + "vào ngày "
                                                                                + schedule.getDepartureDate() + " lúc "
                                                                                + schedule.getDepartureTime()
                                                                                + " mà bạn phụ trách đã bị hủy do toàn bộ khách hàng đã hủy đơn.</p>";
                                                                emailService.sendEmail(emp.getEmail(),
                                                                                "Thông báo hủy lịch trình",
                                                                                content);
                                                        }
                                                        if (systemNotificationService != null
                                                                        && emp.getAccount() != null) {
                                                                systemNotificationService.createNotification(
                                                                                emp.getAccount(),
                                                                                "Tour bị hủy",
                                                                                "Lịch trình tour " + (schedule
                                                                                                .getTour() != null
                                                                                                                ? schedule.getTour()
                                                                                                                                .getTourName()
                                                                                                                : "")
                                                                                                + " vào ngày "
                                                                                                + schedule.getDepartureDate()
                                                                                                + " đã bị hủy do không còn khách.",
                                                                                "TOUR_CANCELLED",
                                                                                "/employee/tours");
                                                        }

                                                        // Tìm một lịch trình khác trong cùng ngày đang
                                                        // thiếu GUIDE để phân công
                                                        List<TourSchedule> otherSchedules = tourScheduleRepository
                                                                        .findAll().stream() // Ideally should use a
                                                                                            // custom query, but this is
                                                                                            // simple enough for demo
                                                                        .filter(s -> s.getDepartureDate() != null && s
                                                                                        .getDepartureDate()
                                                                                        .equals(schedule.getDepartureDate()))
                                                                        .filter(s -> !s.getId()
                                                                                        .equals(schedule.getId()))
                                                                        .filter(s -> "Open".equalsIgnoreCase(
                                                                                        s.getScheduleStatus())
                                                                                        || "Confirmed".equalsIgnoreCase(
                                                                                                        s.getScheduleStatus()))
                                                                        .collect(java.util.stream.Collectors.toList());

                                                        for (TourSchedule other : otherSchedules) {
                                                                List<TourStaffAssignment> otherAssigns = tourStaffAssignmentRepository
                                                                                .findByScheduleId(other.getId());
                                                                boolean hasGuide = false;
                                                                if (otherAssigns != null) {
                                                                        for (TourStaffAssignment oa : otherAssigns) {
                                                                                if ("GUIDE".equalsIgnoreCase(
                                                                                                oa.getStaffRole())) {
                                                                                        hasGuide = true;
                                                                                        break;
                                                                                }
                                                                        }
                                                                }
                                                                if (!hasGuide) {
                                                                        TourStaffAssignment newAssignment = new TourStaffAssignment();
                                                                        newAssignment.setSchedule(other);
                                                                        newAssignment.setEmployee(emp);
                                                                        newAssignment.setStaffRole("GUIDE");
                                                                        tourStaffAssignmentRepository
                                                                                        .save(newAssignment);
                                                                        LOG.info("Đã phân công lại Tour Guide {} cho TourSchedule {} thay thế",
                                                                                        emp.getFullName(),
                                                                                        other.getId());

                                                                        if (emailService != null
                                                                                        && emp.getEmail() != null) {
                                                                                String content = "<h2>Thông báo Phân Công Mới</h2>"
                                                                                                + "<p>Xin chào "
                                                                                                + emp.getFullName()
                                                                                                + ",</p>"
                                                                                                + "<p>Bạn đã được phân công phụ trách lịch trình tour <b>"
                                                                                                + (other.getTour() != null
                                                                                                                ? other.getTour()
                                                                                                                                .getTourName()
                                                                                                                : "")
                                                                                                + "</b> "
                                                                                                + "vào ngày "
                                                                                                + other.getDepartureDate()
                                                                                                + " lúc "
                                                                                                + other.getDepartureTime()
                                                                                                + " thay thế cho lịch trình đã hủy.</p>";
                                                                                emailService.sendEmail(emp.getEmail(),
                                                                                                "Phân công Tour Guide mới",
                                                                                                content);
                                                                        }
                                                                        if (systemNotificationService != null
                                                                                        && emp.getAccount() != null) {
                                                                                systemNotificationService
                                                                                                .createNotification(
                                                                                                                emp.getAccount(),
                                                                                                                "Phân công Tour mới",
                                                                                                                "Bạn được phân công thay thế lịch trình tour "
                                                                                                                                + (other.getTour() != null
                                                                                                                                                ? other.getTour()
                                                                                                                                                                .getTourName()
                                                                                                                                                : "")
                                                                                                                                + " vào ngày "
                                                                                                                                + other.getDepartureDate(),
                                                                                                                "TOUR_ASSIGNED",
                                                                                                                "/employee/tours");
                                                                        }
                                                                        break; // ÄÃ£ tÃ¬m Ä‘Æ°á»£c vÃ  gÃ¡n xong
                                                                }
                                                        }
                                                } else {
                                                        tourStaffAssignmentRepository.delete(a);
                                                }
                                        }
                                }
                        }
                        tourScheduleRepository.save(schedule);
                }

                LOG.info("Cancelled booking {} (resort={}), refund={}, status={}",
                                bookingId, cancelledByResort, refundAmount, newStatus);

                // Gửi email thông báo hủy tour (bất đồng bộ)
                if (emailService != null && booking.getCustomer() != null) {
                        emailService.sendCancellationNotice(
                                        booking, booking.getCustomer(), refundAmount, cancelledByResort, reason);
                }

                return refundAmount;
        }

        @Override
        public BigDecimal cancelTourByCustomer(Long bookingId, Long customerId,
                        com.hoanien.dto.CancelBookingRequestDTO dto) {
                TourBooking booking = tourBookingRepository.findById(bookingId)
                                .orElseThrow(() -> new IllegalStateException("TOUR-002: Booking not found"));

                if (!booking.getCustomer().getId().equals(customerId)) {
                        throw new IllegalStateException("TOUR-003: Không có quyền hủy booking này");
                }

                BigDecimal refundAmount = BigDecimal.ZERO;
                String newStatus = "Cancelled_Refunded";

                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                String formattedTime = java.time.LocalDateTime.now().withNano(0).format(dtf);

                // Trường hợp 1: Tour ghi nợ phòng thực sự (Post to room / Charge to stay Folio)
                boolean isPostToRoom = booking.getNotes() != null && booking.getNotes().contains("paymentMethod=post-room");
                if (isPostToRoom && booking.getRoomBookingDetail() != null) {
                        Long detailId = booking.getRoomBookingDetail().getId();
                        if (folioItemRepository != null) {
                                List<FolioItem> items = folioItemRepository.findByRoomBookingDetailId(detailId);
                                if (items != null) {
                                        String tourName = (booking.getSchedule() != null && booking.getSchedule().getTour() != null)
                                                        ? booking.getSchedule().getTour().getTourName() : "";
                                        for (FolioItem item : items) {
                                                boolean isTourItem = "Tour".equalsIgnoreCase(item.getSourceDepartment())
                                                                || "OTH_TOUR".equalsIgnoreCase(item.getRevenueCode())
                                                                || (item.getDescription() != null && item.getDescription().contains("Tour:"));
                                                if (isTourItem) {
                                                        if (tourName.isEmpty() || (item.getDescription() != null && item.getDescription().contains(tourName))) {
                                                                folioItemRepository.delete(item);
                                                        }
                                                }
                                        }
                                }
                        }
                        newStatus = "Cancelled_Refunded";
                        refundAmount = BigDecimal.ZERO;

                        String roomNum = (booking.getRoomBookingDetail().getRoom() != null) ? booking.getRoomBookingDetail().getRoom().getRoomNumber() : "";
                        String formattedPrice = String.format("%,d", booking.getTotalPrice().longValue()).replace(',', '.');
                        String noteEntry = "[HỦY TOUR GÁN PHÒNG] Đã xóa khoản Tour (" + formattedPrice + " VNĐ) khỏi Folio P." + roomNum + ", khôi phục hạn mức chi tiêu phòng (" + formattedTime + ").";
                        String currentNotes = booking.getNotes() != null ? booking.getNotes().trim() : "";
                        booking.setNotes(currentNotes.isEmpty() ? noteEntry : currentNotes + "\n" + noteEntry);
                } else {
                        // Trường hợp 2: Tour thanh toán trực tiếp (VNPay / Tiền mặt) => Tính mốc 24h
                        newStatus = "Cancelled_Forfeited";
                        if (booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null) {
                                java.time.LocalDateTime now = java.time.LocalDateTime.now().withNano(0);
                                java.time.LocalTime depTime = booking.getSchedule().getDepartureTime() != null
                                                ? booking.getSchedule().getDepartureTime()
                                                : java.time.LocalTime.of(7, 0);
                                java.time.LocalDateTime departureDateTime = booking.getSchedule().getDepartureDate()
                                                .atTime(depTime);
                                long hoursUntilDeparture = java.time.temporal.ChronoUnit.HOURS.between(now, departureDateTime);

                                if (hoursUntilDeparture > 24) {
                                        // Hoàn 50%
                                        refundAmount = booking.getTotalPrice().multiply(new BigDecimal("0.5"));
                                        newStatus = "Cancelled_Refunded";

                                        if (dto != null && dto.getBankName() != null && !dto.getBankName().isEmpty()) {
                                                String phoneNum = (dto.getPhoneNumber() != null && !dto.getPhoneNumber().trim().isEmpty())
                                                                ? dto.getPhoneNumber().trim()
                                                                : (booking.getCustomer() != null ? booking.getCustomer().getPhone() : "N/A");
                                                String refundInfo = String.format(
                                                                "[YÊU CẦU HOÀN TIỀN 50%%] Ngân hàng: %s, STK: %s, Chủ thẻ: %s, SĐT: %s (%s)",
                                                                dto.getBankName(), dto.getAccountNumber(),
                                                                dto.getAccountName(), phoneNum, formattedTime);
                                                String currentNotes = booking.getNotes() != null ? booking.getNotes().trim() : "";
                                                booking.setNotes(currentNotes.isEmpty() ? refundInfo : currentNotes + "\n" + refundInfo);

                                                com.hoanien.models.RefundRequest refund = new com.hoanien.models.RefundRequest();
                                                refund.setTourBooking(booking);
                                                refund.setBankName(dto.getBankName());
                                                refund.setAccountNumber(dto.getAccountNumber());
                                                refund.setAccountName(dto.getAccountName());
                                                refund.setPhoneNumber(phoneNum);
                                                refund.setAmount(refundAmount);
                                                refund.setStatus("Pending");
                                                refundRequestRepository.save(refund);
                                        }
                                } else {
                                        String forfeitInfo = String.format("[HỦY TOUR KHÔNG HOÀN TIỀN] Khách hủy sát giờ (<= 24h), phạt 100%% cọc (%s).", formattedTime);
                                        String currentNotes = booking.getNotes() != null ? booking.getNotes().trim() : "";
                                        booking.setNotes(currentNotes.isEmpty() ? forfeitInfo : currentNotes + "\n" + forfeitInfo);
                                }
                        }
                }

                booking.setBookingStatus(newStatus);
                tourBookingRepository.save(booking);

                if (emailService != null && booking.getCustomer() != null && booking.getCustomer().getEmail() != null) {
                        emailService.sendCancellationNotice(booking, booking.getCustomer(), refundAmount, false, null);
                }

                // Cập nhật số ghế của TourSchedule
                TourSchedule schedule = booking.getSchedule();
                if (schedule != null && booking.getParticipantCount() != null) {
                        int newSeats = schedule.getBookedSeats() - booking.getParticipantCount();
                        if (newSeats < 0)
                                newSeats = 0;
                        schedule.setBookedSeats(newSeats);

                        if (newSeats == 0) {
                                schedule.setScheduleStatus("Cancelled");
                                LOG.info("TourSchedule {} bị hủy vì toàn bộ khách đã hủy (số ghế = 0)",
                                                schedule.getId());

                                // Xóa các phân công nhân viên và thông báo
                                List<TourStaffAssignment> assignments = tourStaffAssignmentRepository
                                                .findByScheduleId(schedule.getId());
                                if (assignments != null && !assignments.isEmpty()) {
                                        for (TourStaffAssignment a : assignments) {
                                                Employee emp = a.getEmployee();
                                                if ("GUIDE".equalsIgnoreCase(a.getStaffRole()) && emp != null) {
                                                        LOG.info("Giải phóng Tour Guide {} khỏi TourSchedule {}",
                                                                        emp.getFullName(), schedule.getId());
                                                        tourStaffAssignmentRepository.delete(a);

                                                        // Thông báo cho nhân viên qua Email
                                                        if (emailService != null && emp.getEmail() != null) {
                                                                String content = "<h2>Thông báo Hủy Lịch Trình</h2>"
                                                                                + "<p>Xin chào " + emp.getFullName()
                                                                                + ",</p>"
                                                                                + "<p>Lịch trình tour <b>"
                                                                                + (schedule.getTour() != null ? schedule
                                                                                                .getTour().getTourName()
                                                                                                : "")
                                                                                + "</b> "
                                                                                + "vào ngày "
                                                                                + schedule.getDepartureDate() + " lúc "
                                                                                + schedule.getDepartureTime()
                                                                                + " mà bạn phụ trách đã bị hủy do toàn bộ khách hàng đã hủy đơn.</p>";
                                                                emailService.sendEmail(emp.getEmail(),
                                                                                "Thông báo hủy lịch trình",
                                                                                content);
                                                        }

                                                        // Tìm một lịch trình khác trong cùng ngày đang
                                                        // thiếu GUIDE để phân công
                                                        List<TourSchedule> otherSchedules = tourScheduleRepository
                                                                        .findAll().stream()
                                                                        .filter(s -> s.getDepartureDate() != null && s
                                                                                        .getDepartureDate()
                                                                                        .equals(schedule.getDepartureDate()))
                                                                        .filter(s -> !s.getId()
                                                                                        .equals(schedule.getId()))
                                                                        .filter(s -> "Open".equalsIgnoreCase(
                                                                                        s.getScheduleStatus())
                                                                                        || "Confirmed".equalsIgnoreCase(
                                                                                                        s.getScheduleStatus()))
                                                                        .collect(java.util.stream.Collectors.toList());

                                                        for (TourSchedule other : otherSchedules) {
                                                                List<TourStaffAssignment> otherAssigns = tourStaffAssignmentRepository
                                                                                .findByScheduleId(other.getId());
                                                                boolean hasGuide = false;
                                                                if (otherAssigns != null) {
                                                                        for (TourStaffAssignment oa : otherAssigns) {
                                                                                if ("GUIDE".equalsIgnoreCase(
                                                                                                oa.getStaffRole())) {
                                                                                        hasGuide = true;
                                                                                        break;
                                                                                }
                                                                        }
                                                                }
                                                                if (!hasGuide) {
                                                                        TourStaffAssignment newAssignment = new TourStaffAssignment();
                                                                        newAssignment.setSchedule(other);
                                                                        newAssignment.setEmployee(emp);
                                                                        newAssignment.setStaffRole("GUIDE");
                                                                        tourStaffAssignmentRepository
                                                                                        .save(newAssignment);
                                                                        LOG.info("Đã phân công lại Tour Guide {} cho TourSchedule {} thay thế",
                                                                                        emp.getFullName(),
                                                                                        other.getId());

                                                                        if (emailService != null
                                                                                        && emp.getEmail() != null) {
                                                                                String content = "<h2>Thông báo Phân Công Mới</h2>"
                                                                                                + "<p>Xin chào "
                                                                                                + emp.getFullName()
                                                                                                + ",</p>"
                                                                                                + "<p>Bạn đã được phân công phụ trách lịch trình tour <b>"
                                                                                                + (other.getTour() != null
                                                                                                                ? other.getTour()
                                                                                                                                .getTourName()
                                                                                                                : "")
                                                                                                + "</b> "
                                                                                                + "vào ngày "
                                                                                                + other.getDepartureDate()
                                                                                                + " lúc "
                                                                                                + other.getDepartureTime()
                                                                                                + " thay thế cho lịch trình đã hủy.</p>";
                                                                                emailService.sendEmail(emp.getEmail(),
                                                                                                "Phân công Tour Guide mới",
                                                                                                content);
                                                                        }
                                                                        break;
                                                                }
                                                        }
                                                } else {
                                                        tourStaffAssignmentRepository.delete(a);
                                                }
                                        }
                                }
                        }
                        tourScheduleRepository.save(schedule);
                }

                LOG.info("Customer {} cancelled booking {}, hours left, refund={}, status={}",
                                customerId, bookingId, refundAmount, newStatus);

                if (emailService != null && booking.getCustomer() != null) {
                        emailService.sendCancellationNotice(booking, booking.getCustomer(), refundAmount, false, null);
                }

                return refundAmount;
        }

        @jakarta.annotation.PostConstruct
        public void clearTourBookingsData() {
                // âš ï¸ ÄÃ£ vÃ´ hiá»‡u hÃ³a: method nÃ y trÆ°á»›c Ä‘Ã¢y xÃ³a toÃ n bá»™
                // Tour_Bookings và Tour_Attendees
                // má»—i láº§n khá»Ÿi Ä‘á»™ng, gÃ¢y máº¥t toÃ n bá»™ seed data. ÄÃ£ comment
                // lại để bảo toàn dữ liệu demo.
                LOG.info("TOUR BOOKINGS DATA CLEANUP COMPLETED SUCCESSFULLY.");
        }
}
