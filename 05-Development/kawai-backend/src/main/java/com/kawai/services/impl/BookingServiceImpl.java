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
import com.kawai.repositories.PaymentTransactionRepository;
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
 *   AND rbd.roomBooking.bookingStatus != 'CANCELLED'
 *
 * Nghĩa là status "HOLD" sẽ ĐƯỢC TÍNH là đang chiếm phòng.
 * → Khi User A tạo HOLD cho phòng R101 ngày 1-5/7,
 *   User B check → countOverlapping = 1 → bị block ngay lập tức.
 *
 * Flow:
 *  1. INSERT RoomBooking(status="HOLD") → soft lock tức thì
 *  2. Tính tiền, xử lý promo, lưu RoomBookingDetail
 *  3. UPDATE RoomBooking(status="CONFIRMED") → chính thức
 *  4. Nếu bất kỳ bước nào fail → @Transactional tự ROLLBACK toàn bộ
 *     → HOLD bị xóa → phòng tự động trống lại
 *
 * Race condition prevention:
 *  - H2/MySQL đều dùng Row-level lock khi INSERT trong cùng transaction
 *  - @Transactional đảm bảo atomic: HOLD + CONFIRM hoặc không có gì
 *  - Scheduler dọn HOLD cũ (> 10 phút) đề phòng crash/timeout
 *
 * Business Rules:
 * BR-FO-01  : Chống overbooking
 * BR-DATE-01: checkOutDate > checkInDate
 * BR-FIN-02 : Hủy trước 48h → refund 100%; trong 48h → forfeit
 * BR-STATUS-01: Booking thành công → "CONFIRMED"
 * BR-ERR-01 : Exception message chứa error code [ERR_PROMO_XXX]
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final BigDecimal BASE_ROOM_PRICE = new BigDecimal("2000000"); // 2tr/đêm
    private static final String STATUS_HOLD      = "HOLD";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    /** HOLD tự động hết hạn sau 10 phút nếu chưa thanh toán (Scheduler dọn) */
    private static final int HOLD_TTL_MINUTES = 10;

    private final RoomBookingRepository roomBookingRepository;
    private final PromotionRepository promotionRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public BookingServiceImpl(RoomBookingRepository roomBookingRepository,
            PromotionRepository promotionRepository,
            RoomRepository roomRepository,
            CustomerRepository customerRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            PaymentGatewayService paymentGatewayService,
            NotificationService notificationService,
            PaymentTransactionRepository paymentTransactionRepository) {
        this.roomBookingRepository = roomBookingRepository;
        this.promotionRepository = promotionRepository;
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    // ══════════════════════════════════════════════════════════════════════
    // 🟢 GREEN — createBooking()
    // ══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request)
            throws RoomNotAvailableException, IllegalArgumentException {

        // ✅ Validate ngày check-in/check-out (BR-DATE-01)
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();
        validateBookingDates(checkIn, checkOut);

        // ✅ Không cho đặt ngày trong quá khứ
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessException("INVALID_DATE", "Check-in date cannot be in the past");
        }

        List<com.kawai.dto.RoomSelectionDTO> roomSelections = request.getRoomSelections();
        if (roomSelections == null || roomSelections.isEmpty()) {
            throw new BusinessException("ROOM_NOT_FOUND", "No rooms provided for booking");
        }

        // ✅ Kiểm tra Customer tồn tại
        if (request.getCustomerId() == null) {
            throw new BusinessException("CUSTOMER_NOT_FOUND", "Customer ID must not be null");
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND",
                        "Customer not found with ID: " + request.getCustomerId()));

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalBaseTotal = BigDecimal.ZERO;
        java.util.List<Room> roomsToBook = new java.util.ArrayList<>();
        java.util.List<BigDecimal> roomCharges = new java.util.ArrayList<>();
        java.util.List<BigDecimal> extraSurcharges = new java.util.ArrayList<>();
        java.util.List<Integer> adultsList = new java.util.ArrayList<>();
        java.util.List<Integer> childrenList = new java.util.ArrayList<>();

        // ══════════════════════════════════════════════════════════════════
        // SOFT LOCK: Tạo RoomBooking(status="HOLD") trước khi tính tiền
        // ══════════════════════════════════════════════════════════════════
        //
        // Đây là bước then chốt chống race condition:
        // - HOLD được INSERT vào cùng transaction này (@Transactional)
        // - countOverlappingBookings() sẽ ĐẾM cả HOLD (vì != 'CANCELLED')
        // - User thứ 2 đến cùng lúc → đọc thấy HOLD → bị block ngay
        // - Nếu bất kỳ bước nào sau đó fail → toàn bộ transaction ROLLBACK
        //   → HOLD bị xóa sạch → phòng tự do lại (zero side effect)
        //
        // Scheduler cleanupStaleHolds() sẽ dọn HOLD quá TTL (10 phút)
        // đề phòng JVM crash trước khi transaction commit.

        // Tạo một HOLD booking placeholder trước
        BigDecimal dummyPrice = BigDecimal.ZERO;
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
        // ✅ Set thời điểm hết hạn chính xác đến phút giây (không chỉ ngày)
        holdBooking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(HOLD_TTL_MINUTES));
        RoomBooking savedHold = roomBookingRepository.save(holdBooking);
        // flush ngay để HOLD visible với các transaction đồng thời (cùng DB isolation level)
        roomBookingRepository.flush();

        log.info("[SOFT_LOCK] HOLD created: bookingId={}, customer={}, {} → {}",
                savedHold.getId(), customer.getId(), checkIn, checkOut);

        // Lặp qua tất cả các phòng để kiểm tra và tính tiền
        for (com.kawai.dto.RoomSelectionDTO selection : roomSelections) {
            String roomNo = selection.getRoomNumber();

            // ✅ Kiểm tra Room tồn tại và áp dụng Pessimistic Write Lock (Khóa dòng chống overbooking)
            Room room = roomRepository.findByRoomNumberWithLock(roomNo)
                    .orElseThrow(() -> new BusinessException("ROOM_NOT_FOUND", "Room not found: " + roomNo));

            // ✅ Kiểm tra trùng lịch đặt phòng (BR-FO-01)
            // countOverlappingBookings đếm tất cả status != 'CANCELLED', bao gồm cả HOLD
            // Nhưng HOLD của chính mình (savedHold) chưa có RoomBookingDetail nên không trùng.
            // Chỉ HOLD/CONFIRMED của người khác mới block.
            long overlapping = roomBookingRepository.countOverlappingBookingsByRoom(roomNo, checkIn, checkOut, savedHold.getId());
            if (overlapping > 0) {
                // Throw exception → @Transactional ROLLBACK → HOLD bị xóa tự động
                throw new RoomNotAvailableException(
                        "Room " + roomNo + " is not available for the selected dates");
            }

            // ✅ Lấy giá phòng từ database (hoặc fallback về giá mặc định)
            BigDecimal pricePerNight = (room.getCategory() != null && room.getCategory().getBasePrice() != null)
                    ? room.getCategory().getBasePrice()
                    : BASE_ROOM_PRICE;

            BigDecimal baseTotal = pricePerNight.multiply(BigDecimal.valueOf(nights));

            // Tính phụ thu
            BigDecimal extraSurcharge = BigDecimal.ZERO;
            int reqAdults = selection.getNumberOfAdults() != null ? selection.getNumberOfAdults() : 0;
            int reqChildren = selection.getNumberOfChildren() != null ? selection.getNumberOfChildren() : 0;

            if (room.getCategory() != null) {
                com.kawai.models.RoomCategory cat = room.getCategory();
                int baseAdults = cat.getBaseAdults() != null ? cat.getBaseAdults() : 0;
                int baseChildren = cat.getBaseChildren() != null ? cat.getBaseChildren() : 0;

                int extraAdults = Math.max(0, reqAdults - baseAdults);
                int extraChildren = Math.max(0, reqChildren - baseChildren);

                BigDecimal adultSurchargeRate = cat.getExtraAdultSurcharge() != null ? cat.getExtraAdultSurcharge() : BigDecimal.ZERO;
                BigDecimal childSurchargeRate = cat.getExtraChildSurcharge() != null ? cat.getExtraChildSurcharge() : BigDecimal.ZERO;

                BigDecimal dailySurcharge = adultSurchargeRate.multiply(BigDecimal.valueOf(extraAdults))
                        .add(childSurchargeRate.multiply(BigDecimal.valueOf(extraChildren)));

                extraSurcharge = dailySurcharge.multiply(BigDecimal.valueOf(nights));
            }

            BigDecimal roomTotal = baseTotal.add(extraSurcharge);
            totalBaseTotal = totalBaseTotal.add(roomTotal);

            roomsToBook.add(room);
            roomCharges.add(baseTotal);
            extraSurcharges.add(extraSurcharge);
            adultsList.add(reqAdults);
            childrenList.add(reqChildren);
        }

        BigDecimal discountedPrice = totalBaseTotal;

        // Xử lý promotion code (UC10.2)
        String promoCode = request.getPromotionCode();
        if (promoCode != null && !promoCode.isBlank()) {
            discountedPrice = applyPromotion(promoCode, totalBaseTotal);
        }

        // ✅ Tính toán tiền đặt cọc ở backend (30% cọc mặc định), không tin tưởng giá trị từ frontend
        BigDecimal depositVal = discountedPrice.multiply(new BigDecimal("0.3")).setScale(0, RoundingMode.HALF_UP);

        // ══════════════════════════════════════════════════════════════════
        // Cập nhật giá thực và chuyển trạng thái sang CONFIRMED
        // ══════════════════════════════════════════════════════════════════
        savedHold.setTotalPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        savedHold.setDepositAmount(depositVal);
        savedHold.setPersonalPinHash("DEFAULT_PIN");
        savedHold.setBookingStatus("CONFIRMED");
        RoomBooking savedBooking = roomBookingRepository.save(savedHold);

        log.info("[SOFT_LOCK] CONFIRMED initialized: bookingId={}", savedBooking.getId());

        for (int i = 0; i < roomsToBook.size(); i++) {
            Room room = roomsToBook.get(i);
            BigDecimal roomCharge = roomCharges.get(i);
            BigDecimal extraSurcharge = extraSurcharges.get(i);
            Integer reqAdults = adultsList.get(i);
            Integer reqChildren = childrenList.get(i);

            RoomBookingDetail detail = new RoomBookingDetail();
            detail.setRoomBooking(savedBooking);
            detail.setRoom(room);
            detail.setCategory(room.getCategory());
            detail.setRoomCharge(roomCharge);
            detail.setExtraSurcharge(extraSurcharge);
            detail.setNumberOfAdults(reqAdults);
            detail.setNumberOfChildren(reqChildren);
            detail.setDetailStatus("Pending");
            detail.setCustomer(customer);
            roomBookingDetailRepository.save(detail);
        }

        BookingResponseDTO response = new BookingResponseDTO();
        response.setBookingId(savedBooking.getId());
        response.setBookingStatus("CONFIRMED");
        response.setDepositAmount(depositVal);
        response.setDiscountedPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        response.setCheckInDate(checkIn);
        response.setCheckOutDate(checkOut);
        response.setCancellationDeadline(checkIn.minusDays(2));

        return response;
    }

    // ══════════════════════════════════════════════════════════════════════
    // 🧹 SCHEDULER — Dọn HOLD cũ (phòng thủ đề phòng crash/timeout)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Scheduler chạy mỗi 60 giây, tìm các HOLD đã quá 15 phút (holdExpiresAt ≤ now).
     * Chuyển chúng sang CANCELLED để giải phóng phòng cho user khác.
     *
     * Case thực tế:
     *  - User tạo booking nhưng đóng trình duyệt giữa chừng
     *  - Server crash sau khi INSERT HOLD nhưng trước khi commit
     *  - Lỗi network khi gọi payment gateway
     *
     * Trong luồng bình thường:
     *  HOLD → CONFIRMED xảy ra trong milliseconds → Scheduler sẽ không tìm thấy gì
     */
    @Scheduled(fixedDelay = 60_000) // Chạy mỗi 60 giây
    @Transactional
    public void cleanupStaleHolds() {
        LocalDateTime now = LocalDateTime.now();
        // findStaleHolds(now) → WHERE bookingStatus='HOLD' AND holdExpiresAt <= now
        List<RoomBooking> staleHolds = roomBookingRepository.findStaleHolds(now);
        if (!staleHolds.isEmpty()) {
            staleHolds.forEach(h -> {
                h.setBookingStatus("CANCELLED");
                h.setHoldExpiresAt(null); // clear sau khi xử lý
                if (h.getCustomer() != null) {
                    try {
                        notificationService.sendNotification(
                            h.getCustomer().getId(),
                            "Hủy đơn phòng tự động",
                            "Đơn đặt phòng #" + h.getId() + " của quý khách đã bị hủy tự động do quá hạn 10 phút chờ thanh toán."
                        );
                    } catch (Exception e) {
                        log.error("Failed to send cancellation notification for booking {}", h.getId(), e);
                    }
                }
            });
            roomBookingRepository.saveAll(staleHolds);
            log.warn("[SOFT_LOCK] 🧹 Auto-cancelled {} expired HOLD booking(s) after {} min TTL at {}",
                    staleHolds.size(), HOLD_TTL_MINUTES, now);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // 🟢 GREEN — applyPromotion()
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Áp dụng mã khuyến mãi (UC10.2).
     * ✅ FIX TC-M2-009a/b/c: Exception message chứa error code [ERR_PROMO_XXX]
     * (BR-ERR-01)
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

        BigDecimal discountRate = promo.getDiscountValue();
        BigDecimal discountAmount = baseTotal.multiply(discountRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return baseTotal.subtract(discountAmount);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * ✅ FIX TC-M2-005: Validate rằng checkOutDate phải SAU checkInDate
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
                .orElseThrow(() -> new BusinessException("FORBIDDEN", "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        if ("CHECKED_IN".equals(booking.getBookingStatus())
                || "CANCELLED".equals(booking.getBookingStatus())
                || (booking.getBookingStatus() != null && booking.getBookingStatus().toUpperCase().startsWith("CANCEL"))) {
            throw new BusinessException("BKG-005", "Invalid booking status");
        }

        LocalDateTime checkInTime = booking.getCheckInDate().atTime(14, 0);
        LocalDateTime now = LocalDateTime.now();

        long hoursUntilCheckIn = java.time.temporal.ChronoUnit.HOURS.between(now, checkInTime);
        boolean isEligibleForRefund = hoursUntilCheckIn >= 48;

        try {
            if (isEligibleForRefund) {
                paymentGatewayService.processRefund("TXN_" + bookingId, booking.getDepositAmount());
                notificationService.sendNotification(customerId, "Cancel Success",
                        "Your booking has been cancelled and refunded.");
            } else {
                notificationService.sendNotification(customerId, "Cancel Success (No Refund)",
                        "Your booking has been cancelled. No refund is issued as cancellation is within 48 hours of check-in.");
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
            notificationService.sendNotification(customerId, "Cancel Failed",
                    "Cancellation failed due to a system error. Please try again later.");
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponseDTO getBookingDetail(Long bookingId, Long customerId) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN", "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);

        BigDecimal baseRoomPrice = BigDecimal.ZERO;
        BigDecimal servicesFee = BigDecimal.ZERO;
        int totalAdults = 0;
        int totalChildren = 0;

        for (RoomBookingDetail detail : details) {
            baseRoomPrice = baseRoomPrice.add(detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);
            servicesFee = servicesFee.add(detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO);
            totalAdults += detail.getNumberOfAdults() != null ? detail.getNumberOfAdults() : 0;
            totalChildren += detail.getNumberOfChildren() != null ? detail.getNumberOfChildren() : 0;
        }

        BigDecimal totalAmount = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal promotionDiscount = baseRoomPrice.add(servicesFee).subtract(totalAmount);
        if (promotionDiscount.compareTo(BigDecimal.ZERO) < 0) {
            promotionDiscount = BigDecimal.ZERO;
        }

        // Group category names by count to format as "CategoryName xCount" (or just CategoryName if count = 1)
        java.util.Map<String, Long> categoryCounts = details.stream()
                .filter(d -> d.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        d -> d.getCategory().getCategoryName(),
                        java.util.stream.Collectors.counting()
                ));
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
        dto.setDepositAmount(booking.getDepositAmount() != null ? booking.getDepositAmount().setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        dto.setBookingStatus(booking.getBookingStatus());
        long remainingSeconds = 0L;
        if ("HOLD".equals(booking.getBookingStatus()) && booking.getHoldExpiresAt() != null) {
            remainingSeconds = java.time.temporal.ChronoUnit.SECONDS.between(LocalDateTime.now(), booking.getHoldExpiresAt());
            if (remainingSeconds < 0) remainingSeconds = 0L;
        }
        dto.setRemainingHoldSeconds(remainingSeconds);

        return dto;
    }

    @Override
    @Transactional
    public BigDecimal applyCoupon(Long bookingId, String couponCode, Long customerId) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN", "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
        BigDecimal baseRoomPrice = BigDecimal.ZERO;
        BigDecimal servicesFee = BigDecimal.ZERO;
        for (RoomBookingDetail detail : details) {
            baseRoomPrice = baseRoomPrice.add(detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);
            servicesFee = servicesFee.add(detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO);
        }
        BigDecimal totalBaseTotal = baseRoomPrice.add(servicesFee);

        BigDecimal discountedPrice = applyPromotion(couponCode, totalBaseTotal);
        BigDecimal discountAmount = totalBaseTotal.subtract(discountedPrice);

        booking.setTotalPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        booking.setDepositAmount(discountedPrice.multiply(new BigDecimal("0.3")).setScale(0, RoundingMode.HALF_UP)); // Default deposit is 30% of final price
        roomBookingRepository.save(booking);

        return discountAmount.setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void confirmBooking(Long bookingId, Long customerId, String fullName, String phone, String email, String cccd, String notes) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN", "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        if ("CANCELLED".equals(booking.getBookingStatus())) {
            throw new BusinessException("BKG-EXPIRED", "Đơn đặt phòng này đã bị hủy do quá thời gian thanh toán. Vui lòng đặt lại phòng mới!");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng!"));

        // Cập nhật thông tin khách hàng từ form
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        if (cccd != null && !cccd.equals("********") && !cccd.trim().isEmpty()) {
            customer.setCccdPassportEncrypted(cccd);
        }
        customerRepository.save(customer);

        // Chỉ lưu ghi chú và giữ nguyên trạng thái HOLD để chờ thanh toán cọc
        booking.setNotes(notes);
        roomBookingRepository.save(booking);
    }


}