package com.kawai.services.impl;

import com.kawai.dto.BookingRequestDTO;
import com.kawai.dto.BookingResponseDTO;
import com.kawai.dto.BookingDetailResponseDTO;
import com.kawai.exceptions.RoomNotAvailableException;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.Promotion;
import com.kawai.models.RoomBooking;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.Room;
import com.kawai.models.Customer;
import com.kawai.repositories.PromotionRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.CustomerRepository;

import com.kawai.services.interfaces.BookingService;
import com.kawai.models.PaymentTransaction;
import com.kawai.models.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kawai.services.interfaces.PaymentGatewayService;
import com.kawai.services.interfaces.NotificationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * BookingServiceImpl — UC10: Đặt phòng & Thanh toán cọc trực tuyến
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Soft Lock Strategy — dùng chính bảng Room_Bookings với status "HOLD":
 *
 * Tại sao không cần bảng mới?
 * ─────────────────────────────────────────────────────────────────────────
 * Query countOverlappingBookings() đã có điều kiện:
 * AND rbd.roomBooking.bookingStatus != 'CANCELLED'
 *
 * Nghĩa là status "HOLD" sẽ ĐƯỢC TÍNH là đang chiếm phòng.
 * → Khi User A tạo HOLD cho phòng R101 ngày 1-5/7,
 * User B check → countOverlapping = 1 → bị block ngay lập tức.
 *
 * Flow:
 * 1. INSERT RoomBooking(status="HOLD") → soft lock tức thì
 * 2. Tính tiền, xử lý promo, lưu RoomBookingDetail
 * 3. UPDATE RoomBooking(status="CONFIRMED") → chính thức
 * 4. Nếu bất kỳ bước nào fail → @Transactional tự ROLLBACK toàn bộ
 * → HOLD bị xóa → phòng tự động trống lại
 *
 * Race condition prevention:
 * - H2/MySQL đều dùng Row-level lock khi INSERT trong cùng transaction
 * - @Transactional đảm bảo atomic: HOLD + CONFIRM hoặc không có gì
 * - Scheduler dọn HOLD cũ (> 10 phút) đề phòng crash/timeout
 *
 * Business Rules:
 * BR-FO-01 : Chống overbooking
 * BR-DATE-01: checkOutDate > checkInDate
 * BR-FIN-02 : Hủy trước 48h → refund 100%; trong 48h → forfeit
 * BR-STATUS-01: Booking thành công → "CONFIRMED"
 * BR-ERR-01 : Exception message chứa error code [ERR_PROMO_XXX]
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final BigDecimal BASE_ROOM_PRICE = new BigDecimal("2000000"); // 2tr/đêm
    private static final String STATUS_HOLD = "HOLD";
    private static final int HOLD_TTL_MINUTES = 10;

    private final RoomBookingRepository roomBookingRepository;
    private final PromotionRepository promotionRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final com.kawai.repositories.RoomCategoryRepository roomCategoryRepository;
    private final com.kawai.repositories.RoomSurchargeRepository roomSurchargeRepository;
    private final com.kawai.repositories.DependentRepository dependentRepository;
    private final com.kawai.repositories.RoomGuestRepository roomGuestRepository;

    public BookingServiceImpl(RoomBookingRepository roomBookingRepository,
            PromotionRepository promotionRepository,
            RoomRepository roomRepository,
            CustomerRepository customerRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            PaymentGatewayService paymentGatewayService,
            NotificationService notificationService,
            com.kawai.repositories.RoomCategoryRepository roomCategoryRepository,
            com.kawai.repositories.RoomSurchargeRepository roomSurchargeRepository,
            com.kawai.repositories.DependentRepository dependentRepository,
            com.kawai.repositories.RoomGuestRepository roomGuestRepository) {
        this.roomBookingRepository = roomBookingRepository;
        this.promotionRepository = promotionRepository;
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
        this.roomCategoryRepository = roomCategoryRepository;
        this.roomSurchargeRepository = roomSurchargeRepository;
        this.dependentRepository = dependentRepository;
        this.roomGuestRepository = roomGuestRepository;
    }

    // ══════════════════════════════════════════════════════════════════════
    // 🟢 GREEN — createBooking()
    // ══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request)
            throws RoomNotAvailableException, IllegalArgumentException {

        // Validate ngày check-in/check-out (BR-DATE-01)
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();
        validateBookingDates(checkIn, checkOut);

        // Không cho đặt ngày trong quá khứ
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessException("INVALID_DATE", "Check-in date cannot be in the past");
        }

        List<com.kawai.dto.RoomSelectionDTO> roomSelections = request.getRoomSelections();
        if (roomSelections == null || roomSelections.isEmpty()) {
            throw new BusinessException("ROOM_NOT_FOUND", "No rooms provided for booking");
        }

        // Đảm bảo mỗi selection có CategoryName bằng cách truy vấn từ room nếu chưa có
        // (TDD fix)
        for (com.kawai.dto.RoomSelectionDTO selection : roomSelections) {
            if (selection.getCategoryName() == null || selection.getCategoryName().trim().isEmpty()) {
                if (selection.getRoomNumber() != null) {
                    Room r = roomRepository.findByRoomNumber(selection.getRoomNumber()).orElse(null);
                    if (r != null && r.getCategory() != null) {
                        selection.setCategoryName(r.getCategory().getCategoryName());
                    }
                }
            }
        }

        // Kiểm tra Customer tồn tại
        if (request.getCustomerId() == null) {
            throw new BusinessException("CUSTOMER_NOT_FOUND", "Customer ID must not be null");
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                        "Customer not found with ID: " + request.getCustomerId()));

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalBaseTotal = BigDecimal.ZERO;
        java.util.List<com.kawai.models.RoomCategory> categoriesToBook = new java.util.ArrayList<>();
        java.util.List<BigDecimal> roomCharges = new java.util.ArrayList<>();
        java.util.List<BigDecimal> extraSurcharges = new java.util.ArrayList<>();
        java.util.List<Integer> adultsList = new java.util.ArrayList<>();
        java.util.List<Integer> childrenList = new java.util.ArrayList<>();
        java.util.List<java.util.List<Integer>> childrenAgesList = new java.util.ArrayList<>();

        // ══════════════════════════════════════════════════════════════════
        // SOFT LOCK: Tạo RoomBooking(status="HOLD") trước khi tính tiền
        // ══════════════════════════════════════════════════════════════════
        // Tạo một HOLD booking placeholder trước

        RoomBooking holdBooking = new RoomBooking();
        holdBooking.setCustomer(customer);
        holdBooking.setBookingDate(LocalDate.now());
        holdBooking.setTotalPrice(BigDecimal.ZERO);
        holdBooking.setBookingStatus(STATUS_HOLD);
        holdBooking.setBookingSource("Direct_Web");
        holdBooking.setCheckInDate(checkIn);
        holdBooking.setCheckOutDate(checkOut);
        holdBooking.setDepositAmount(BigDecimal.ZERO);
        holdBooking.setCancellationDeadline(checkIn.minusDays(2));
        holdBooking.setPersonalPinHash("HOLD_PENDING");
        holdBooking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(HOLD_TTL_MINUTES));
        RoomBooking savedHold = roomBookingRepository.save(holdBooking);
        roomBookingRepository.flush();

        log.info("[SOFT_LOCK] HOLD created: bookingId={}, customer={}, {} → {}",
                savedHold.getId(), customer.getId(), checkIn, checkOut);

        // Nhóm các phòng được chọn theo Hạng Phòng
        java.util.Map<String, java.util.List<com.kawai.dto.RoomSelectionDTO>> groupedSelections = roomSelections
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(com.kawai.dto.RoomSelectionDTO::getCategoryName));

        // Lặp qua từng Hạng Phòng để kiểm tra và tính tiền
        for (java.util.Map.Entry<String, java.util.List<com.kawai.dto.RoomSelectionDTO>> entry : groupedSelections
                .entrySet()) {
            String catName = entry.getKey();
            java.util.List<com.kawai.dto.RoomSelectionDTO> selections = entry.getValue();
            int requestedQty = selections.size();

            // Áp dụng Pessimistic Lock (Khóa dòng chống overbooking) trên Hạng Phòng
            com.kawai.models.RoomCategory category = roomCategoryRepository.findByCategoryNameWithLock(catName)
                    .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found: " + catName));

            // Tính toán số phòng không bị trùng
            long totalRooms = roomRepository.countActiveRoomsByCategoryName(catName);
            long overlapping = roomBookingRepository.countOverlappingBookingsByCategory(catName, checkIn, checkOut,
                    savedHold.getId());
            long available = totalRooms - overlapping;

            if (available < requestedQty) {
                // Throw exception → @Transactional ROLLBACK → HOLD bị xóa tự động
                throw new RoomNotAvailableException(
                        "Hạng phòng " + catName + " chỉ còn trống " + available + " phòng.");
            }

            // Lấy giá hạng phòng từ database
            BigDecimal pricePerNight = category.getBasePrice() != null ? category.getBasePrice() : BASE_ROOM_PRICE;
            BigDecimal baseTotal = pricePerNight.multiply(BigDecimal.valueOf(nights));

            for (com.kawai.dto.RoomSelectionDTO selection : selections) {
                // Tính phụ thu
                BigDecimal extraSurcharge = BigDecimal.ZERO;
                int reqAdults = selection.getNumberOfAdults() != null ? selection.getNumberOfAdults() : 0;
                int reqChildren = selection.getNumberOfChildren() != null ? selection.getNumberOfChildren() : 0;

                int baseAdults = category.getBaseAdults() != null ? category.getBaseAdults() : 0;
                int baseChildren = category.getBaseChildren() != null ? category.getBaseChildren() : 0;

                int extraAdults = Math.max(0, reqAdults - baseAdults);

                // Child surcharge calculation using exact ages
                BigDecimal childSurchargeTotal = BigDecimal.ZERO;
                java.util.List<Integer> ages = selection.getChildrenAges() != null
                        ? new java.util.ArrayList<>(selection.getChildrenAges())
                        : new java.util.ArrayList<>();
                java.util.Collections.sort(ages); // Sort ascending (youngest first)
                int chargeableChildrenCount = Math.max(0, reqChildren - baseChildren);
                int skipCount = Math.max(0, reqChildren - chargeableChildrenCount); // Free allowance for youngest

                for (int idx = skipCount; idx < ages.size(); idx++) {
                    int childAge = ages.get(idx);
                    BigDecimal surcharge = roomSurchargeRepository.findSurchargeForAge(category, childAge)
                            .map(com.kawai.models.RoomSurcharge::getPriceModifier)
                            .orElse(BigDecimal.ZERO);
                    childSurchargeTotal = childSurchargeTotal.add(surcharge);
                }

                BigDecimal adultSurchargeRate = category.getExtraAdultSurcharge() != null
                        ? category.getExtraAdultSurcharge()
                        : BigDecimal.ZERO;

                BigDecimal dailySurcharge = adultSurchargeRate.multiply(BigDecimal.valueOf(extraAdults))
                        .add(childSurchargeTotal);

                extraSurcharge = dailySurcharge.multiply(BigDecimal.valueOf(nights));

                BigDecimal roomTotal = baseTotal.add(extraSurcharge);
                totalBaseTotal = totalBaseTotal.add(roomTotal);

                categoriesToBook.add(category);
                roomCharges.add(baseTotal);
                extraSurcharges.add(extraSurcharge);
                adultsList.add(reqAdults);
                childrenList.add(reqChildren);
                childrenAgesList.add(ages);
            }
        }

        BigDecimal discountedPrice = totalBaseTotal;
        String promoCode = request.getPromotionCode();
        if (promoCode != null && !promoCode.isBlank()) {
            discountedPrice = applyPromotion(promoCode, totalBaseTotal);
        }

        // Tính toán tiền đặt cọc ở backend (30% cọc mặc định)
        BigDecimal depositVal = discountedPrice.multiply(new BigDecimal("0.3")).setScale(0, RoundingMode.HALF_UP);

        // ══════════════════════════════════════════════════════════════════
        // Cập nhật giá thực và chuyển trạng thái sang CONFIRMED
        // ══════════════════════════════════════════════════════════════════
        savedHold.setTotalPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        savedHold.setDepositAmount(depositVal);
        savedHold.setPersonalPinHash("DEFAULT_PIN");
        savedHold.setBookingStatus(STATUS_HOLD);
        RoomBooking savedBooking = roomBookingRepository.save(savedHold);

        log.info("[SOFT_LOCK] HOLD updated with details: bookingId={}", savedBooking.getId());

        for (int i = 0; i < categoriesToBook.size(); i++) {
            com.kawai.models.RoomCategory category = categoriesToBook.get(i);
            BigDecimal roomCharge = roomCharges.get(i);
            BigDecimal extraSurcharge = extraSurcharges.get(i);
            Integer reqAdults = adultsList.get(i);
            Integer reqChildren = childrenList.get(i);

            RoomBookingDetail detail = new RoomBookingDetail();
            detail.setRoomBooking(savedBooking);
            detail.setRoom(null); // Không chốt cứng phòng, để trống cho lễ tân tự chia
            detail.setCategory(category);
            detail.setRoomCharge(roomCharge);
            detail.setExtraSurcharge(extraSurcharge);
            detail.setNumberOfAdults(reqAdults);
            detail.setNumberOfChildren(reqChildren);
            detail.setDetailStatus("Pending");
            detail.setCustomer(customer);
            roomBookingDetailRepository.save(detail);

            // Create RoomGuest for Adults (Stub)
            for (int a = 0; a < reqAdults; a++) {
                com.kawai.models.RoomGuest guest = new com.kawai.models.RoomGuest();
                guest.setRoomBookingDetail(detail);
                guest.setGuestType("ADULT");
                if (a == 0 && i == 0) {
                    guest.setCustomer(customer);
                    guest.setIsPrimaryContact(true);
                } else {
                    com.kawai.models.Dependent stubDep = new com.kawai.models.Dependent();
                    stubDep.setCustomer(customer);
                    stubDep.setBirthDate(java.time.LocalDate.now().minusYears(18).withDayOfYear(1));
                    dependentRepository.save(stubDep);

                    guest.setCustomer(null);
                    guest.setDependent(stubDep);
                    guest.setIsPrimaryContact(false);
                }
                roomGuestRepository.save(guest);
            }

            // Create Dependent and RoomGuest for Children
            java.util.List<Integer> agesForRoom = childrenAgesList.get(i);
            for (Integer age : agesForRoom) {
                com.kawai.models.Dependent dep = new com.kawai.models.Dependent();
                dep.setCustomer(customer);
                // Calculate approximate birthDate from age (e.g., Jan 1st of birth year)
                dep.setBirthDate(java.time.LocalDate.now().minusYears(age).withDayOfYear(1));
                dependentRepository.save(dep);

                com.kawai.models.RoomGuest guest = new com.kawai.models.RoomGuest();
                guest.setRoomBookingDetail(detail);
                guest.setGuestType("CHILD");
                guest.setDependent(dep);
                guest.setIsPrimaryContact(false);
                roomGuestRepository.save(guest);
            }
        }

        BookingResponseDTO response = new BookingResponseDTO();
        response.setBookingId(savedBooking.getId());
        response.setBookingStatus(STATUS_HOLD);
        response.setDepositAmount(depositVal);
        response.setDiscountedPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        response.setCheckInDate(checkIn);
        response.setCheckOutDate(checkOut);
        response.setCancellationDeadline(checkIn.minusDays(2));

        return response;
    }

    /**
     * Scheduler chạy mỗi 60 giây, tìm các HOLD đã quá 10 phút (holdExpiresAt ≤
     * now).
     * Chuyển chúng sang CANCELLED để giải phóng phòng cho user khác.
     */
    @Scheduled(fixedDelay = 60_000) // Chạy mỗi 60 giây
    @Transactional
    public void cleanupStaleHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<RoomBooking> staleHolds = roomBookingRepository.findStaleHolds(now);
        if (!staleHolds.isEmpty()) {
            staleHolds.forEach(h -> {
                h.setBookingStatus("CANCELLED");
                h.setHoldExpiresAt(null);
                if (h.getCustomer() != null) {
                    try {
                        notificationService.sendNotification(
                                h.getCustomer().getId(),
                                "Hủy đơn phòng tự động",
                                "Đơn đặt phòng #" + h.getId()
                                        + " của quý khách đã bị hủy tự động do quá hạn 10 phút chờ thanh toán.");
                    } catch (Exception e) {
                        log.error("Failed to send cancellation notification for booking {}", h.getId(), e);
                    }
                }
            });
            roomBookingRepository.saveAll(staleHolds);
            log.warn("[SOFT_LOCK] Auto-cancelled {} expired HOLD booking(s) after {} min TTL at {}",
                    staleHolds.size(), HOLD_TTL_MINUTES, now);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 🟢 GREEN — applyPromotion()
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Áp dụng mã khuyến mãi (UC10.2).
     */
    private BigDecimal applyPromotion(String promoCode, BigDecimal baseTotal) {
        Promotion promo = promotionRepository.findByPromoCode(promoCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Promotion code '" + promoCode + "' does not exist [ERR_PROMO_NOT_FOUND]"));

        if (!Boolean.TRUE.equals(promo.getIsActive())) {
            throw new IllegalArgumentException(
                    "Promotion code '" + promoCode + "' is invalid or expired [ERR_PROMO_INACTIVE]");
        }

        if (promo.getValidTo().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Promotion code '" + promoCode + "' has expired [ERR_PROMO_EXPIRED]");
        }

        BigDecimal discountValue = promo.getDiscountValue();
        BigDecimal finalPrice;

        if (discountValue.compareTo(new BigDecimal("100")) <= 0) {
            BigDecimal discountAmount = baseTotal.multiply(discountValue)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            finalPrice = baseTotal.subtract(discountAmount);
        } else {
            finalPrice = baseTotal.subtract(discountValue);
        }
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        return finalPrice;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * FIX TC-M2-005: Validate rằng checkOutDate phải SAU checkInDate
     * (BR-DATE-01).
     */
    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkOut == null || checkIn == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out date must be after check-in date");
        }
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId, Long customerId) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN",
                        "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        String status = booking.getBookingStatus() != null ? booking.getBookingStatus().toUpperCase() : "";
        if ("CHECKED_IN".equals(status)
                || "CANCELLED".equals(status)
                || status.startsWith("CANCEL")) {
            throw new BusinessException("BKG-005", "Invalid booking status");
        }

        // Nếu booking chưa đóng tiền cọc (depositAmount = null hoặc = 0 hoặc status là
        // HOLD)
        if (booking.getDepositAmount() == null || booking.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            booking.setBookingStatus("CANCELLED");
            roomBookingRepository.save(booking);
            BookingResponseDTO response = new BookingResponseDTO();
            response.setBookingId(bookingId);
            response.setBookingStatus("CANCELLED");
            response.setDepositAmount(BigDecimal.ZERO);
            return response;
        }

        // Đã đóng cọc -> Cần check deadline hoàn tiền sử dụng cancellationDeadline
        LocalDate today = LocalDate.now();
        boolean isEligibleForRefund = booking.getCancellationDeadline() != null
                && !today.isAfter(booking.getCancellationDeadline());

        try {
            if (isEligibleForRefund) {
                if (paymentGatewayService != null) {
                    paymentGatewayService.processRefund("TXN_" + bookingId, booking.getDepositAmount());
                }
                if (notificationService != null) {
                    notificationService.sendNotification(customerId, "Cancel Success",
                            "Your booking has been cancelled and refunded.");
                }
            } else {
                if (notificationService != null) {
                    notificationService.sendNotification(customerId, "Cancel Success (No Refund)",
                            "Your booking has been cancelled. No refund is issued as cancellation is within 48 hours of check-in.");
                }
            }

            String newStatus = isEligibleForRefund ? "Cancelled_Refunded" : "Cancelled_Forfeited";
            booking.setBookingStatus(newStatus);
            roomBookingRepository.save(booking);

            BookingResponseDTO response = new BookingResponseDTO();
            response.setBookingId(bookingId);
            response.setBookingStatus(newStatus);
            response.setDepositAmount(isEligibleForRefund ? booking.getDepositAmount() : BigDecimal.ZERO);
            return response;
        } catch (Exception e) {
            if (notificationService != null) {
                notificationService.sendNotification(customerId, "Cancel Failed",
                        "Cancellation failed due to a system error. Please try again later.");
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponseDTO getBookingDetail(Long bookingId, Long customerId) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN",
                        "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);

        BigDecimal baseRoomPrice = BigDecimal.ZERO;
        BigDecimal servicesFee = BigDecimal.ZERO;
        int totalAdults = 0;
        int totalChildren = 0;

        for (RoomBookingDetail detail : details) {
            baseRoomPrice = baseRoomPrice
                    .add(detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);
            servicesFee = servicesFee
                    .add(detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO);
            totalAdults += detail.getNumberOfAdults() != null ? detail.getNumberOfAdults() : 0;
            totalChildren += detail.getNumberOfChildren() != null ? detail.getNumberOfChildren() : 0;
        }

        BigDecimal totalAmount = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal promotionDiscount = baseRoomPrice.add(servicesFee).subtract(totalAmount);
        if (promotionDiscount.compareTo(BigDecimal.ZERO) < 0) {
            promotionDiscount = BigDecimal.ZERO;
        }

        // Group category names by count to format as "CategoryName xCount" (or just
        // CategoryName if count = 1)
        java.util.Map<String, Long> categoryCounts = details.stream()
                .filter(d -> d.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        d -> d.getCategory().getCategoryName(),
                        java.util.stream.Collectors.counting()));
        List<String> roomCategories = categoryCounts.entrySet().stream()
                .map(entry -> entry.getValue() > 1 ? entry.getKey() + " x" + entry.getValue() : entry.getKey())
                .collect(java.util.stream.Collectors.toList());

        BookingDetailResponseDTO dto = new BookingDetailResponseDTO();
        dto.setRoomCategories(roomCategories);
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalAdults(totalAdults);
        dto.setTotalChildren(totalChildren);
        dto.setBaseRoomPrice(baseRoomPrice.setScale(0, RoundingMode.HALF_UP));
        dto.setServicesFee(servicesFee.setScale(0, RoundingMode.HALF_UP));
        dto.setPromotionDiscount(promotionDiscount.setScale(0, RoundingMode.HALF_UP));
        dto.setTotalAmount(totalAmount.setScale(0, RoundingMode.HALF_UP));
        dto.setDepositAmount(
                booking.getDepositAmount() != null ? booking.getDepositAmount().setScale(0, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

        dto.setBookingStatus(booking.getBookingStatus());
        long remainingSeconds = 0L;
        if ("HOLD".equals(booking.getBookingStatus()) && booking.getHoldExpiresAt() != null) {
            remainingSeconds = java.time.temporal.ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    booking.getHoldExpiresAt());
            if (remainingSeconds < 0)
                remainingSeconds = 0L;
        }
        dto.setRemainingHoldSeconds(remainingSeconds);

        return dto;
    }

    @Override
    @Transactional
    public BigDecimal applyCoupon(Long bookingId, String couponCode, Long customerId) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN",
                        "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
        BigDecimal baseRoomPrice = BigDecimal.ZERO;
        BigDecimal servicesFee = BigDecimal.ZERO;
        for (RoomBookingDetail detail : details) {
            baseRoomPrice = baseRoomPrice
                    .add(detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);
            servicesFee = servicesFee
                    .add(detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO);
        }
        BigDecimal totalBaseTotal = baseRoomPrice.add(servicesFee);

        BigDecimal discountedPrice = applyPromotion(couponCode, totalBaseTotal);
        BigDecimal discountAmount = totalBaseTotal.subtract(discountedPrice);

        booking.setTotalPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        booking.setDepositAmount(discountedPrice.multiply(new BigDecimal("0.3")).setScale(0, RoundingMode.HALF_UP)); // Default
                                                                                                                     // deposit
                                                                                                                     // is
                                                                                                                     // 30%
                                                                                                                     // of
                                                                                                                     // final
                                                                                                                     // price
        roomBookingRepository.save(booking);

        return discountAmount.setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void confirmBooking(Long bookingId, Long customerId, String fullName, String phone, String email,
            String cccd, String notes) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN",
                        "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        if ("CANCELLED".equals(booking.getBookingStatus())) {
            throw new BusinessException("BKG-EXPIRED",
                    "Đơn đặt phòng này đã bị hủy do quá thời gian thanh toán. Vui lòng đặt lại phòng mới!");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng!"));

        // Cập nhật thông tin khách hàng từ form
        customer.setFullName(fullName);
        if (!com.kawai.utils.ValidationUtils.isValidPhone(phone)) {
            throw new BusinessException("INVALID_PHONE",
                    "Số điện thoại không hợp lệ (Phải gồm 10 số và bắt đầu bằng 0)");
        }
        customer.setPhone(phone);
        customer.setEmail(email);
        if (cccd != null && !cccd.equals("********") && !cccd.trim().isEmpty()) {
            if (!com.kawai.utils.ValidationUtils.isValidDocument(cccd)) {
                throw new BusinessException("INVALID_CCCD", "CCCD/Passport không hợp lệ (Phải là CCCD 12 số, hoặc Passport 8-12 ký tự có chứa chữ cái)");
            }
            customer.setCccdPassportEncrypted(com.kawai.utils.EncryptionUtils.encrypt(cccd.trim()));
        }
        customerRepository.save(customer);

        // Chỉ lưu ghi chú và giữ nguyên trạng thái HOLD để chờ thanh toán cọc
        booking.setNotes(notes);
        roomBookingRepository.save(booking);
    }

}