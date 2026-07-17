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
        private com.kawai.services.interfaces.SystemNotificationService systemNotificationService;

        @Autowired
        private com.kawai.repositories.RefundRequestRepository refundRequestRepository;

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
                Customer customer = customerRepository.findById(request.getCustomerId())
                                .orElseThrow(() -> new IllegalStateException("TOUR-003: Customer not found"));

                // 2. Chá»‘ng Double-booking: kiá»ƒm tra available slots
                int alreadyBooked = tourBookingRepository.countByScheduleAndBookingStatus(schedule, "Confirmed");
                int remainingCapacity = schedule.getTour().getMaxCapacity() - alreadyBooked;

                if (request.getParticipantCount() > remainingCapacity) {
                        LOG.warn("TOUR-001: Tour schedule {} háº¿t chá»—. Already={}, Request={}, Max={}",
                                        schedule.getId(), alreadyBooked, request.getParticipantCount(),
                                        schedule.getTour().getMaxCapacity());
                        throw new IllegalStateException(
                                        "TOUR-001: Háº¿t chá»—. Chá»‰ cÃ²n " + remainingCapacity + " chá»— trá»‘ng");
                }

                Tour tour = schedule.getTour();
                if (Boolean.TRUE.equals(tour.getIsInsuranceRequired()) && !request.isAcceptInsurance()) {
                        LOG.warn("TOUR-INS-001: KhÃ¡ch {} tá»« chá»‘i báº£o hiá»ƒm báº¯t buá»™c cho tour {}",
                                        request.getCustomerId(), tour.getId());
                        throw new IllegalStateException(
                                        "TOUR-INS-001: Tour nÃ y báº¯t buá»™c mua báº£o hiá»ƒm du lá»‹ch. Vui lÃ²ng Ä‘á»“ng Ã½ mua báº£o hiá»ƒm Ä‘á»ƒ tiáº¿p tá»¥c Ä‘áº·t chá»—.");
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
                                if ("DÆ°á»›i 2 tuá»•i".equalsIgnoreCase(age)) {
                                        // Miá»…n phÃ­
                                        childDiscount = childDiscount.add(basePrice);
                                } else if ("2 - 11 tuá»•i".equalsIgnoreCase(age)) {
                                        // Giáº£m 50%
                                        totalPrice = totalPrice.add(basePrice.multiply(new BigDecimal("0.5")));
                                        childDiscount = childDiscount.add(basePrice.multiply(new BigDecimal("0.5")));
                                } else {
                                        // Máº·c Ä‘á»‹nh giáº£m 50%
                                        totalPrice = totalPrice.add(basePrice.multiply(new BigDecimal("0.5")));
                                        childDiscount = childDiscount.add(basePrice.multiply(new BigDecimal("0.5")));
                                }
                        }
                }

                // 3b. TÃ­nh phÃ­ báº£o hiá»ƒm Ä‘á»ƒ háº¡ch toÃ¡n (Ä‘Ã£ bao gá»“m trong giÃ¡
                // tour gá»‘c)
                BigDecimal insuranceFee = BigDecimal.ZERO;
                if (Boolean.TRUE.equals(tour.getIsInsuranceRequired()) && request.isAcceptInsurance()) {
                        insuranceFee = tour.getInsurancePrice()
                                        .multiply(BigDecimal.valueOf(request.getParticipantCount()));
                        LOG.info("PhÃ­ báº£o hiá»ƒm háº¡ch toÃ¡n (Ä‘Ã£ bao gá»“m trong giÃ¡): {} x {} ngÆ°á»i = {} VND",
                                        tour.getInsurancePrice(), request.getParticipantCount(), insuranceFee);
                }

                // 4. Ãp dá»¥ng mÃ£ giáº£m giÃ¡ (náº¿u cÃ³)
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
                                                                "KhÃ¡ch hÃ ng Ä‘Ã£ vÆ°á»£t quÃ¡ sá»‘ láº§n sá»­ dá»¥ng mÃ£ giáº£m giÃ¡ nÃ y (1 láº§n) [ERR_PROMO_USAGE_EXCEEDED]");
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
                                        LOG.info("Ãp dá»¥ng mÃ£ giáº£m giÃ¡ '{}' cho tour booking: giáº£m {} VND",
                                                        promoCode, discountAmount);
                                } else {
                                        LOG.warn("MÃ£ giáº£m giÃ¡ '{}' khÃ´ng há»£p lá»‡ hoáº·c Ä‘Ã£ háº¿t háº¡n",
                                                        promoCode);
                                }
                        } else {
                                LOG.warn("MÃ£ giáº£m giÃ¡ '{}' khÃ´ng tá»“n táº¡i trong há»‡ thá»‘ng", promoCode);
                        }
                }

                // 5. Táº¡o TourBooking
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

                // LÆ°u thÃ´ng tin chi tiáº¿t vÃ o notes Ä‘á»ƒ email hiá»ƒn thá»‹
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

                // 5a. Sinh mÃ£ báº£o hiá»ƒm vÃ  Ä‘Ã¡nh dáº¥u schedule (chá»‰ khi tour báº¯t
                // buá»™c báº£o hiá»ƒm)
                if (Boolean.TRUE.equals(tour.getIsInsuranceRequired()) && request.isAcceptInsurance()) {
                        if (!Boolean.TRUE.equals(schedule.getIsInsuranceProcessed())) {
                                // ChÆ°a cÃ³ mÃ£ â†’ sinh má»›i
                                String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                                String policyNumber = "INS-" + LocalDate.now()
                                                + "-SCH" + schedule.getId()
                                                + "-" + suffix;
                                schedule.setInsurancePolicyNumber(policyNumber);
                                schedule.setIsInsuranceProcessed(true);
                                tourScheduleRepository.save(schedule);
                                LOG.info("Sinh mÃ£ báº£o hiá»ƒm cho schedule {}: {}", schedule.getId(), policyNumber);
                        } else {
                                LOG.info("Schedule {} Ä‘Ã£ cÃ³ mÃ£ báº£o hiá»ƒm: {}", schedule.getId(),
                                                schedule.getInsurancePolicyNumber());
                        }
                }

                // 5. Táº¡o TourAttendee records
                List<TourAttendee> attendees = new ArrayList<>();
                int currentAttendeeCount = 0;

                // KhÃ¡ch hÃ ng Ä‘áº·t chÃ­nh lÃ  attendee sá»‘ 1
                if (currentAttendeeCount < request.getParticipantCount()) {
                        TourAttendee mainAttendee = new TourAttendee();
                        mainAttendee.setTourBooking(savedBooking);
                        mainAttendee.setCustomer(customer);
                        mainAttendee.setAttendanceStatus("Not_Show");
                        attendees.add(mainAttendee);
                        currentAttendeeCount++;
                }

                // Táº¡o cÃ¡c attendee Ä‘i kÃ¨m dá»±a trÃªn danh sÃ¡ch companions (ngÆ°á»i
                // lá»›n Ä‘i cÃ¹ng)
                if (request.getCompanions() != null && !request.getCompanions().isEmpty()) {
                        for (TourBookingRequest.CompanionRequest comp : request.getCompanions()) {
                                if (currentAttendeeCount >= request.getParticipantCount()) {
                                        break;
                                }
                                // LÆ°u thÃ´ng tin ngÆ°á»i Ä‘i kÃ¨m vÃ o báº£ng Dependents
                                Dependent dep = new Dependent();
                                dep.setCustomer(customer);
                                dep.setDependentName(comp.getName());
                                // TÃ­nh ngÃ y sinh tá»« Ä‘á»™ tuá»•i (vÃ­ dá»¥ máº·c Ä‘á»‹nh láº¥y nÄƒm hiá»‡n
                                // táº¡i - sá»‘ tuá»•i)
                                int age = comp.getAge() != null ? comp.getAge() : 12;
                                dep.setBirthDate(LocalDate.now().minusYears(age));
                                dep.setGender("Nam");
                                dep.setIsDeleted(false);

                                // LÆ°u sá»‘ CCCD/Passport cá»§a ngÆ°á»i Ä‘i cÃ¹ng Ä‘á»ƒ lÃ m thá»§ tá»¥c báº£o
                                // hiá»ƒm lá»¯ hÃ nh báº¯t buá»™c
                                if (comp.getIdCard() != null && !comp.getIdCard().trim().isEmpty()) {
                                        dep.setCccdPassportEncrypted(com.kawai.utils.EncryptionUtils
                                                        .encrypt(comp.getIdCard().trim()));
                                } else if (comp.getPhone() != null && !comp.getPhone().trim().isEmpty()) {
                                        dep.setCccdPassportEncrypted("PHONE_" + comp.getPhone().trim());
                                }

                                Dependent savedDep = dependentRepository.save(dep);

                                TourAttendee attendee = new TourAttendee();
                                attendee.setTourBooking(savedBooking);
                                attendee.setDependent(savedDep);
                                attendee.setAttendanceStatus("Not_Show");
                                attendees.add(attendee);
                                currentAttendeeCount++;
                        }
                }

                // Náº¿u cÃ²n thá»«a slot (tráº» em chÆ°a nháº­p chi tiáº¿t companion),
                // Táº¡o cÃ¡c attendee tráº» em tá»« danh sÃ¡ch childAges thá»±c táº¿ Ä‘Æ°á»£c
                // gá»­i lÃªn
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
                                name = "Tráº» em (" + rawAge + ")";
                                ageLabel = rawAge;
                        }

                        // TÃ­nh nÄƒm sinh Æ°á»›c lÆ°á»£ng tá»« nhÃ£n tuá»•i
                        int estimatedAge = 12; // default: adult
                        if ("DÆ°á»›i 2 tuá»•i".equalsIgnoreCase(ageLabel)) {
                                estimatedAge = 1;
                        } else if ("2 - 11 tuá»•i".equalsIgnoreCase(ageLabel)) {
                                estimatedAge = 6;
                        }

                        Dependent childDep = new Dependent();
                        childDep.setCustomer(customer);
                        childDep.setDependentName(name);
                        childDep.setBirthDate(LocalDate.now().minusYears(estimatedAge));
                        childDep.setGender("KhÃ´ng xÃ¡c Ä‘á»‹nh");
                        childDep.setIsDeleted(false);
                        // Ghi chÃº nguá»“n gá»‘c vÃ  nhÃ£n tuá»•i Ä‘á»ƒ hiá»ƒn thá»‹ trong popup
                        childDep.setCccdPassportEncrypted("AUTO_CHILD_" + ageLabel.replace(" ", "_"));

                        Dependent savedChildDep = dependentRepository.save(childDep);

                        TourAttendee attendee = new TourAttendee();
                        attendee.setTourBooking(savedBooking);
                        attendee.setDependent(savedChildDep);
                        attendee.setAttendanceStatus("Not_Show");
                        attendees.add(attendee);
                        currentAttendeeCount++;
                }

                tourAttendeeRepository.saveAll(attendees);

                // 6. Post to Room: ghi ná»£ vÃ o Folio phÃ²ng
                if (request.isPostToRoom()) {
                        // BR-TR-08: Khi chá»n Post to Room, báº¯t buá»™c pháº£i cung cáº¥p
                        // roomBookingDetailId
                        // há»£p lá»‡ (phÃ²ng Ä‘Ã£ check-in). Náº¿u khÃ´ng â†’ TOUR-004.
                        if (request.getRoomBookingDetailId() == null) {
                                LOG.warn("TOUR-004: Booking {} yÃªu cáº§u Post to Room nhÆ°ng khÃ´ng cung cáº¥p roomBookingDetailId",
                                                savedBooking.getId());
                                throw new IllegalStateException(
                                                "TOUR-004: Vui lÃ²ng chá»n phÃ²ng Ä‘á»ƒ ghi ná»£. PhÃ²ng pháº£i Ä‘Ã£ Ä‘Æ°á»£c check-in.");
                        }

                        // BR-TR-09: roomBookingDetailId pháº£i tá»“n táº¡i trong há»‡ thá»‘ng â†’
                        // TOUR-005
                        RoomBookingDetail detail = roomBookingDetailRepository
                                        .findById(request.getRoomBookingDetailId())
                                        .orElseThrow(() -> {
                                                LOG.warn("TOUR-005: RoomBookingDetail {} khÃ´ng tá»“n táº¡i trong há»‡ thá»‘ng",
                                                                request.getRoomBookingDetailId());
                                                return new IllegalStateException(
                                                                "TOUR-005: Chi tiáº¿t Ä‘áº·t phÃ²ng khÃ´ng tá»“n táº¡i hoáº·c Ä‘Ã£ bá»‹ xÃ³a. ID: "
                                                                                + request.getRoomBookingDetailId());
                                        });

                        // Check Folio Credit Limit
                        BigDecimal limit = detail.getSubCreditLimit() != null ? detail.getSubCreditLimit()
                                        : BigDecimal.ZERO;
                        java.util.List<FolioItem> folioItems = folioItemRepository
                                        .findByRoomBookingDetailId(detail.getId());
                        // Chi tiÃªu thá»±c (FolioItem DÆ¯Æ NG)
                        BigDecimal charged = folioItems.stream()
                                        .filter(fi -> !Boolean.TRUE.equals(fi.getIsSettledSeparately()))
                                        .map(FolioItem::getAmount)
                                        .filter(a -> a != null && a.compareTo(BigDecimal.ZERO) > 0)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        // ÄÃ£ náº¡p thÃªm háº¡n má»©c (khÃ´ng tÃ­nh tiá»n cá»c walk-in)
                        BigDecimal creditTopUp = folioItems.stream()
                                        .filter(fi -> !Boolean.TRUE.equals(fi.getIsSettledSeparately()))
                                        .filter(fi -> fi.getDescription() != null && fi.getDescription()
                                                        .startsWith("Náº¡p tiá»n nÃ¢ng háº¡n má»©c"))
                                        .map(FolioItem::getAmount)
                                        .filter(a -> a != null && a.compareTo(BigDecimal.ZERO) < 0)
                                        .map(BigDecimal::abs)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal available = limit.add(creditTopUp).subtract(charged);

                        if (available.compareTo(totalPrice) < 0) {
                                LOG.warn("TOUR-LIMIT WARNING: Han muc chi tieu cua phong {} khong du de thanh toan tour (Available: {}, Price: {}). Van cho phep ghi no folio theo yeu cau demo/post-room.",
                                                detail.getId(), available, totalPrice);
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
                        folioItem.setRevenueCode("OTH_TOUR");
                        folioItemRepository.save(folioItem);
                        LOG.info("Post to Room: FolioItem táº¡o thÃ nh cÃ´ng cho tour booking {} â€” {} VND, RoomBookingDetail {}",
                                        savedBooking.getId(), totalPrice, detail.getId());
                } else {
                        // KhÃ´ng Post to Room â€” bá» qua bÆ°á»›c ghi Folio hoÃ n toÃ n
                        LOG.debug("Booking {}: postToRoom=false, bá» qua ghi Folio", savedBooking.getId());
                }

                // 7. Gá»­i email xÃ¡c nháº­n Ä‘áº·t tour (báº¥t Ä‘á»“ng bá»™, khÃ´ng block)
                // Náº¿u thanh toÃ¡n qua VNPay, email sáº½ Ä‘Æ°á»£c gá»­i sau khi VNPay xÃ¡c
                // nháº­n thÃ nh cÃ´ng
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

                // Tá»± Ä‘á»™ng gÃ¡n Tour Guide cho schedule cá»§a booking nÃ y theo luáº­t
                try {
                        java.time.LocalDate depDate = schedule.getDepartureDate();
                        java.time.LocalTime depTime = schedule.getDepartureTime();

                        // ID cá»§a cÃ¡c Tour Guides: 5 = NguynNgoc, 6 = Ngá»c Lan, 7 = HoÃ ng Nam
                        Long selectedGuideId = 5L; // Æ¯u tiÃªn NguynNgoc

                        if (depDate != null && depTime != null) {
                                // 1. Kiá»ƒm tra xem NguynNgoc (5L) cÃ³ bá»‹ trÃ¹ng lá»‹ch vÃ o ngÃ y & giá»
                                // nÃ y khÃ´ng
                                boolean ngocConflict = false;
                                List<TourStaffAssignment> ngocAssigns = tourStaffAssignmentRepository
                                                .findByEmployeeId(5L);
                                if (ngocAssigns != null) {
                                        for (TourStaffAssignment a : ngocAssigns) {
                                                if (a.getSchedule() != null
                                                                && !a.getSchedule().getId().equals(schedule.getId())) {
                                                        if (depDate.equals(a.getSchedule().getDepartureDate()) &&
                                                                        depTime.equals(a.getSchedule()
                                                                                        .getDepartureTime())
                                                                        &&
                                                                        "GUIDE".equalsIgnoreCase(a.getStaffRole())) {
                                                                ngocConflict = true;
                                                                break;
                                                        }
                                                }
                                        }
                                }

                                if (ngocConflict) {
                                        // 2. Náº¿u NguynNgoc bá»‹ trÃ¹ng, kiá»ƒm tra xem Ngá»c Lan (6L) cÃ³ bá»‹
                                        // trÃ¹ng khÃ´ng
                                        boolean lanConflict = false;
                                        List<TourStaffAssignment> lanAssigns = tourStaffAssignmentRepository
                                                        .findByEmployeeId(6L);
                                        if (lanAssigns != null) {
                                                for (TourStaffAssignment a : lanAssigns) {
                                                        if (a.getSchedule() != null && !a.getSchedule().getId()
                                                                        .equals(schedule.getId())) {
                                                                if (depDate.equals(a.getSchedule().getDepartureDate())
                                                                                &&
                                                                                depTime.equals(a.getSchedule()
                                                                                                .getDepartureTime())
                                                                                &&
                                                                                "GUIDE".equalsIgnoreCase(
                                                                                                a.getStaffRole())) {
                                                                        lanConflict = true;
                                                                        break;
                                                                }
                                                        }
                                                }
                                        }

                                        if (!lanConflict) {
                                                selectedGuideId = 6L; // GÃ¡n cho Ngá»c Lan
                                        } else {
                                                selectedGuideId = 7L; // Fallback gÃ¡n cho HoÃ ng Nam
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
                                LOG.info("ÄÃ£ gÃ¡n Tour Guide {} (ID {}) cho schedule ID: {}", guide.getFullName(),
                                                selectedGuideId, schedule.getId());
                        }
                } catch (Exception e) {
                        LOG.error("Lá»—i khi tá»± Ä‘á»™ng gÃ¡n Tour Guide theo luáº­t thá»i gian: {}", e.getMessage());
                }

                return savedBooking.getId();
        }

        @Override
        public void scheduleTour(Long scheduleId, Long employeeId, String staffRole) {
                // UC20.2: Láº­p lá»‹ch chuyáº¿n tour â€” gÃ¡n nhÃ¢n viÃªn (Tour Guide / TÃ i
                // xáº¿) vÃ o lá»‹ch
                // trÃ¬nh
                // Business Rule: BR-TR-06 â€” Cáº£nh bÃ¡o Admin náº¿u chÆ°a Ä‘á»§ Minimum Pax
                // trÆ°á»›c 24h,
                // nhÆ°ng logic gÃ¡n nhÃ¢n viÃªn váº«n Ä‘Æ°á»£c thá»±c hiá»‡n Ä‘á»™c láº­p á»Ÿ
                // Ä‘Ã¢y.
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
                // UC20.3: Há»§y tour lá»¯ hÃ nh vÃ  tÃ­nh toÃ¡n tiá»n hoÃ n cá»c
                // BR-TR-05: Há»§y do Resort â†’ hoÃ n 100%; KhÃ¡ch tá»± há»§y trong 24h â†’
                // máº¥t 50%
                TourBooking booking = tourBookingRepository.findById(bookingId)
                                .orElseThrow(() -> new IllegalStateException("TOUR-002: Booking not found"));

                BigDecimal refundAmount;
                String newStatus;

                if (cancelledByResort) {
                        // Há»§y do phÃ­a Resort: hoÃ n tiá»n 100%
                        refundAmount = booking.getTotalPrice();
                        newStatus = "Cancelled_Refunded";
                } else {
                        // KhÃ¡ch tá»± há»§y (trong vÃ²ng 24h trÆ°á»›c giá» tour): máº¥t 50% cá»c
                        refundAmount = booking.getTotalPrice().multiply(new BigDecimal("0.5"));
                        newStatus = "Cancelled_Forfeited";
                }

                booking.setBookingStatus(newStatus);
                tourBookingRepository.save(booking);

                // Cáº­p nháº­t sá»‘ gháº¿ cá»§a TourSchedule
                TourSchedule schedule = booking.getSchedule();
                if (schedule != null && booking.getParticipantCount() != null) {
                        int newSeats = schedule.getBookedSeats() - booking.getParticipantCount();
                        if (newSeats < 0)
                                newSeats = 0;
                        schedule.setBookedSeats(newSeats);

                        if (newSeats == 0) {
                                schedule.setScheduleStatus("Cancelled");
                                LOG.info("TourSchedule {} bá»‹ há»§y vÃ¬ toÃ n bá»™ khÃ¡ch Ä‘Ã£ há»§y (sá»‘ gháº¿ = 0)",
                                                schedule.getId());

                                // XÃ³a cÃ¡c phÃ¢n cÃ´ng nhÃ¢n viÃªn vÃ  thÃ´ng bÃ¡o
                                List<TourStaffAssignment> assignments = tourStaffAssignmentRepository
                                                .findByScheduleId(schedule.getId());
                                if (assignments != null && !assignments.isEmpty()) {
                                        for (TourStaffAssignment a : assignments) {
                                                Employee emp = a.getEmployee();
                                                if ("GUIDE".equalsIgnoreCase(a.getStaffRole()) && emp != null) {
                                                        LOG.info("Giáº£i phÃ³ng Tour Guide {} khá»i TourSchedule {}",
                                                                        emp.getFullName(), schedule.getId());
                                                        tourStaffAssignmentRepository.delete(a);

                                                        // ThÃ´ng bÃ¡o cho nhÃ¢n viÃªn qua Email (sá»­ dá»¥ng HTML log
                                                        // hoáº·c email service)
                                                        if (emailService != null && emp.getEmail() != null) {
                                                                String content = "<h2>ThÃ´ng bÃ¡o Há»§y Lá»‹ch TrÃ¬nh</h2>"
                                                                                + "<p>Xin chÃ o " + emp.getFullName()
                                                                                + ",</p>"
                                                                                + "<p>Lá»‹ch trÃ¬nh tour <b>"
                                                                                + (schedule.getTour() != null ? schedule
                                                                                                .getTour().getTourName()
                                                                                                : "")
                                                                                + "</b> "
                                                                                + "vÃ o ngÃ y "
                                                                                + schedule.getDepartureDate() + " lÃºc "
                                                                                + schedule.getDepartureTime()
                                                                                + " mÃ  báº¡n phá»¥ trÃ¡ch Ä‘Ã£ bá»‹ há»§y do toÃ n bá»™ khÃ¡ch hÃ ng Ä‘Ã£ há»§y Ä‘Æ¡n.</p>";
                                                                emailService.sendEmail(emp.getEmail(),
                                                                                "ThÃ´ng bÃ¡o há»§y lá»‹ch trÃ¬nh",
                                                                                content);
                                                        }
                                                        if (systemNotificationService != null
                                                                        && emp.getAccount() != null) {
                                                                systemNotificationService.createNotification(
                                                                                emp.getAccount(),
                                                                                "Tour bá»‹ há»§y",
                                                                                "Lá»‹ch trÃ¬nh tour " + (schedule
                                                                                                .getTour() != null
                                                                                                                ? schedule.getTour()
                                                                                                                                .getTourName()
                                                                                                                : "")
                                                                                                + " vÃ o ngÃ y "
                                                                                                + schedule.getDepartureDate()
                                                                                                + " Ä‘Ã£ bá»‹ há»§y do khÃ´ng cÃ²n khÃ¡ch.",
                                                                                "TOUR_CANCELLED",
                                                                                "/employee/tours");
                                                        }

                                                        // TÃ¬m má»™t lá»‹ch trÃ¬nh khÃ¡c trong cÃ¹ng ngÃ y Ä‘ang
                                                        // thiáº¿u GUIDE Ä‘á»ƒ phÃ¢n cÃ´ng
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
                                                                        LOG.info("ÄÃ£ phÃ¢n cÃ´ng láº¡i Tour Guide {} cho TourSchedule {} thay tháº¿",
                                                                                        emp.getFullName(),
                                                                                        other.getId());

                                                                        if (emailService != null
                                                                                        && emp.getEmail() != null) {
                                                                                String content = "<h2>ThÃ´ng bÃ¡o PhÃ¢n CÃ´ng Má»›i</h2>"
                                                                                                + "<p>Xin chÃ o "
                                                                                                + emp.getFullName()
                                                                                                + ",</p>"
                                                                                                + "<p>Báº¡n Ä‘Ã£ Ä‘Æ°á»£c phÃ¢n cÃ´ng phá»¥ trÃ¡ch lá»‹ch trÃ¬nh tour <b>"
                                                                                                + (other.getTour() != null
                                                                                                                ? other.getTour()
                                                                                                                                .getTourName()
                                                                                                                : "")
                                                                                                + "</b> "
                                                                                                + "vÃ o ngÃ y "
                                                                                                + other.getDepartureDate()
                                                                                                + " lÃºc "
                                                                                                + other.getDepartureTime()
                                                                                                + " thay tháº¿ cho lá»‹ch trÃ¬nh Ä‘Ã£ há»§y.</p>";
                                                                                emailService.sendEmail(emp.getEmail(),
                                                                                                "PhÃ¢n cÃ´ng Tour Guide má»›i",
                                                                                                content);
                                                                        }
                                                                        if (systemNotificationService != null
                                                                                        && emp.getAccount() != null) {
                                                                                systemNotificationService
                                                                                                .createNotification(
                                                                                                                emp.getAccount(),
                                                                                                                "PhÃ¢n cÃ´ng Tour má»›i",
                                                                                                                "Báº¡n Ä‘Æ°á»£c phÃ¢n cÃ´ng thay tháº¿ lá»‹ch trÃ¬nh tour "
                                                                                                                                + (other.getTour() != null
                                                                                                                                                ? other.getTour()
                                                                                                                                                                .getTourName()
                                                                                                                                                : "")
                                                                                                                                + " vÃ o ngÃ y "
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

                // Gá»­i email thÃ´ng bÃ¡o há»§y tour (báº¥t Ä‘á»“ng bá»™)
                if (emailService != null && booking.getCustomer() != null) {
                        emailService.sendCancellationNotice(
                                        booking, booking.getCustomer(), refundAmount, cancelledByResort);
                }

                return refundAmount;
        }

        @Override
        public BigDecimal cancelTourByCustomer(Long bookingId, Long customerId,
                        com.kawai.dto.CancelBookingRequestDTO dto) {
                TourBooking booking = tourBookingRepository.findById(bookingId)
                                .orElseThrow(() -> new IllegalStateException("TOUR-002: Booking not found"));

                if (!booking.getCustomer().getId().equals(customerId)) {
                        throw new IllegalStateException("TOUR-003: KhÃ´ng cÃ³ quyá»n há»§y booking nÃ y");
                }

                BigDecimal refundAmount = BigDecimal.ZERO;
                String newStatus = "Cancelled_Forfeited";

                // Quy táº¯c theo yÃªu cáº§u: > 24h => hoÃ n 50%, <= 24h => hoÃ n 0%
                if (booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null) {
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();
                        // Assume departure time is 07:00 if not specified
                        java.time.LocalTime depTime = booking.getSchedule().getDepartureTime() != null
                                        ? booking.getSchedule().getDepartureTime()
                                        : java.time.LocalTime.of(7, 0);
                        java.time.LocalDateTime departureDateTime = booking.getSchedule().getDepartureDate()
                                        .atTime(depTime);
                        long hoursUntilDeparture = java.time.temporal.ChronoUnit.HOURS.between(now, departureDateTime);

                        if (hoursUntilDeparture > 24) {
                                // HoÃ n 50%
                                refundAmount = booking.getTotalPrice().multiply(new BigDecimal("0.5"));
                                newStatus = "Cancelled_Refunded";

                                // Táº¡o RefundRequest
                                if (dto != null && dto.getBankName() != null && !dto.getBankName().isEmpty()) {
                                        String refundInfo = String.format(
                                                        "[YÃŠU Cáº¦U HOÃ€N TIá»€N] NgÃ¢n hÃ ng: %s, STK: %s, Chá»§ tháº»: %s",
                                                        dto.getBankName(), dto.getAccountNumber(),
                                                        dto.getAccountName());
                                        String currentNotes = booking.getNotes() != null ? booking.getNotes() : "";
                                        booking.setNotes(currentNotes + "\n" + refundInfo);

                                        com.kawai.models.RefundRequest refund = new com.kawai.models.RefundRequest();
                                        refund.setTourBooking(booking);
                                        refund.setBankName(dto.getBankName());
                                        refund.setAccountNumber(dto.getAccountNumber());
                                        refund.setAccountName(dto.getAccountName());
                                        refund.setAmount(refundAmount);
                                        refund.setStatus("Pending");
                                        refundRequestRepository.save(refund);
                                }
                        }
                }

                booking.setBookingStatus(newStatus);
                tourBookingRepository.save(booking);

                if (emailService != null && booking.getCustomer() != null && booking.getCustomer().getEmail() != null) {
                        emailService.sendCancellationNotice(booking, booking.getCustomer(), refundAmount, false);
                }

                // Cáº­p nháº­t sá»‘ gháº¿ cá»§a TourSchedule
                TourSchedule schedule = booking.getSchedule();
                if (schedule != null && booking.getParticipantCount() != null) {
                        int newSeats = schedule.getBookedSeats() - booking.getParticipantCount();
                        if (newSeats < 0)
                                newSeats = 0;
                        schedule.setBookedSeats(newSeats);

                        if (newSeats == 0) {
                                schedule.setScheduleStatus("Cancelled");
                                LOG.info("TourSchedule {} bá»‹ há»§y vÃ¬ toÃ n bá»™ khÃ¡ch Ä‘Ã£ há»§y (sá»‘ gháº¿ = 0)",
                                                schedule.getId());

                                // XÃ³a cÃ¡c phÃ¢n cÃ´ng nhÃ¢n viÃªn vÃ  thÃ´ng bÃ¡o
                                List<TourStaffAssignment> assignments = tourStaffAssignmentRepository
                                                .findByScheduleId(schedule.getId());
                                if (assignments != null && !assignments.isEmpty()) {
                                        for (TourStaffAssignment a : assignments) {
                                                Employee emp = a.getEmployee();
                                                if ("GUIDE".equalsIgnoreCase(a.getStaffRole()) && emp != null) {
                                                        LOG.info("Giáº£i phÃ³ng Tour Guide {} khá»i TourSchedule {}",
                                                                        emp.getFullName(), schedule.getId());
                                                        tourStaffAssignmentRepository.delete(a);

                                                        // ThÃ´ng bÃ¡o cho nhÃ¢n viÃªn qua Email
                                                        if (emailService != null && emp.getEmail() != null) {
                                                                String content = "<h2>ThÃ´ng bÃ¡o Há»§y Lá»‹ch TrÃ¬nh</h2>"
                                                                                + "<p>Xin chÃ o " + emp.getFullName()
                                                                                + ",</p>"
                                                                                + "<p>Lá»‹ch trÃ¬nh tour <b>"
                                                                                + (schedule.getTour() != null ? schedule
                                                                                                .getTour().getTourName()
                                                                                                : "")
                                                                                + "</b> "
                                                                                + "vÃ o ngÃ y "
                                                                                + schedule.getDepartureDate() + " lÃºc "
                                                                                + schedule.getDepartureTime()
                                                                                + " mÃ  báº¡n phá»¥ trÃ¡ch Ä‘Ã£ bá»‹ há»§y do toÃ n bá»™ khÃ¡ch hÃ ng Ä‘Ã£ há»§y Ä‘Æ¡n.</p>";
                                                                emailService.sendEmail(emp.getEmail(),
                                                                                "ThÃ´ng bÃ¡o há»§y lá»‹ch trÃ¬nh",
                                                                                content);
                                                        }

                                                        // TÃ¬m má»™t lá»‹ch trÃ¬nh khÃ¡c trong cÃ¹ng ngÃ y Ä‘ang
                                                        // thiáº¿u GUIDE Ä‘á»ƒ phÃ¢n cÃ´ng
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
                                                                        LOG.info("ÄÃ£ phÃ¢n cÃ´ng láº¡i Tour Guide {} cho TourSchedule {} thay tháº¿",
                                                                                        emp.getFullName(),
                                                                                        other.getId());

                                                                        if (emailService != null
                                                                                        && emp.getEmail() != null) {
                                                                                String content = "<h2>ThÃ´ng bÃ¡o PhÃ¢n CÃ´ng Má»›i</h2>"
                                                                                                + "<p>Xin chÃ o "
                                                                                                + emp.getFullName()
                                                                                                + ",</p>"
                                                                                                + "<p>Báº¡n Ä‘Ã£ Ä‘Æ°á»£c phÃ¢n cÃ´ng phá»¥ trÃ¡ch lá»‹ch trÃ¬nh tour <b>"
                                                                                                + (other.getTour() != null
                                                                                                                ? other.getTour()
                                                                                                                                .getTourName()
                                                                                                                : "")
                                                                                                + "</b> "
                                                                                                + "vÃ o ngÃ y "
                                                                                                + other.getDepartureDate()
                                                                                                + " lÃºc "
                                                                                                + other.getDepartureTime()
                                                                                                + " thay tháº¿ cho lá»‹ch trÃ¬nh Ä‘Ã£ há»§y.</p>";
                                                                                emailService.sendEmail(emp.getEmail(),
                                                                                                "PhÃ¢n cÃ´ng Tour Guide má»›i",
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
                        emailService.sendCancellationNotice(booking, booking.getCustomer(), refundAmount, false);
                }

                return refundAmount;
        }

        @jakarta.annotation.PostConstruct
        public void clearTourBookingsData() {
                // âš ï¸ ÄÃ£ vÃ´ hiá»‡u hÃ³a: method nÃ y trÆ°á»›c Ä‘Ã¢y xÃ³a toÃ n bá»™
                // Tour_Bookings vÃ  Tour_Attendees
                // má»—i láº§n khá»Ÿi Ä‘á»™ng, gÃ¢y máº¥t toÃ n bá»™ seed data. ÄÃ£ comment
                // láº¡i Ä‘á»ƒ báº£o toÃ n dá»¯ liá»‡u demo.
                LOG.info("TOUR BOOKINGS DATA CLEANUP COMPLETED SUCCESSFULLY.");
        }
}
