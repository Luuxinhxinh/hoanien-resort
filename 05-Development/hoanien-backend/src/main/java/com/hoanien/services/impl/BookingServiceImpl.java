package com.hoanien.services.impl;

import com.hoanien.dto.BookingRequestDTO;
import com.hoanien.dto.BookingResponseDTO;
import com.hoanien.dto.BookingDetailResponseDTO;
import com.hoanien.exceptions.RoomNotAvailableException;
import com.hoanien.exceptions.BusinessException;
import com.hoanien.models.Promotion;
import com.hoanien.models.RoomBooking;
import com.hoanien.models.RoomBookingDetail;
import com.hoanien.models.Room;
import com.hoanien.models.Customer;
import com.hoanien.models.Workflow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoanien.repositories.PromotionRepository;
import com.hoanien.repositories.RoomBookingRepository;
import com.hoanien.repositories.RoomBookingDetailRepository;
import com.hoanien.repositories.RoomRepository;
import com.hoanien.repositories.CustomerRepository;
import com.hoanien.repositories.WorkflowRepository;
import com.hoanien.services.interfaces.BookingService;
import com.hoanien.services.interfaces.WorkflowEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hoanien.services.interfaces.NotificationService;
import com.hoanien.services.interfaces.EmailService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * BookingServiceImpl — UC10: Đặt phòng & Thanh toán cọc trực tuyến
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Soft Lock Strategy — dùng chính bảng Room_Bookings với status
 * "Pending_Payment":
 *
 * Tại sao không cần bảng mới?
 * ─────────────────────────────────────────────────────────────────────────
 * Query countOverlappingBookings() đã có điều kiện:
 * AND rbd.roomBooking.bookingStatus != 'CANCELLED'
 *
 * Nghĩa là status "Pending_Payment" sẽ ĐƯỢC TÍNH là đang chiếm phòng
 * (SoftLock).
 * Flow:
 * 1. INSERT RoomBooking(status="Pending_Payment") + holdExpiresAt=now+2min →
 * soft lock tức thì
 * 2. Tính tiền, xử lý promo, lưu RoomBookingDetail
 * 3. VNPay IPN callback → UPDATE RoomBooking(status="Confirmed") → chính thức
 * 4. Nếu quá 2 phút chưa thanh toán → cleanupStaleHolds() (Scheduler mỗi phút)
 * tự XÓA
 * → Pending_Payment bị xóa → phòng tự động trống lại
 *
 * Race condition prevention:
 * - H2/MySQL đều dùng Row-level lock khi INSERT trong cùng transaction
 * - @Transactional đảm bảo atomic: Pending_Payment + Confirmed hoặc không có gì
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final BigDecimal BASE_ROOM_PRICE = new BigDecimal("2000000");

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private com.hoanien.services.interfaces.PricingService pricingService;

    @Autowired
    private com.hoanien.repositories.BookingRepository bookingRepository;

    @Autowired
    private com.hoanien.repositories.TourBookingRepository tourBookingRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    private final RoomBookingRepository roomBookingRepository;
    private final PromotionRepository promotionRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final NotificationService notificationService;
    private final com.hoanien.repositories.RoomCategoryRepository roomCategoryRepository;
    private final com.hoanien.repositories.RoomSurchargeRepository roomSurchargeRepository;
    private final com.hoanien.repositories.DependentRepository dependentRepository;
    private final com.hoanien.repositories.RoomGuestRepository roomGuestRepository;
    private final EmailService emailService;

    @Autowired
    private com.hoanien.repositories.RefundRequestRepository refundRequestRepository;

    @Autowired
    private com.hoanien.repositories.FolioItemRepository folioItemRepository;

    public BookingServiceImpl(RoomBookingRepository roomBookingRepository,
            PromotionRepository promotionRepository,
            RoomRepository roomRepository,
            CustomerRepository customerRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            NotificationService notificationService,
            EmailService emailService,
            com.hoanien.repositories.RoomCategoryRepository roomCategoryRepository,
            com.hoanien.repositories.RoomSurchargeRepository roomSurchargeRepository,
            com.hoanien.repositories.DependentRepository dependentRepository,
            com.hoanien.repositories.RoomGuestRepository roomGuestRepository,
            @org.springframework.context.annotation.Lazy WorkflowEngineService workflowEngineService) {
        this.roomBookingRepository = roomBookingRepository;
        this.promotionRepository = promotionRepository;
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.roomCategoryRepository = roomCategoryRepository;
        this.roomSurchargeRepository = roomSurchargeRepository;
        this.dependentRepository = dependentRepository;
        this.roomGuestRepository = roomGuestRepository;
        this.workflowEngineService = workflowEngineService;
    }

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

        List<com.hoanien.dto.RoomSelectionDTO> roomSelections = request.getRoomSelections();
        if (roomSelections == null || roomSelections.isEmpty()) {
            throw new BusinessException("ROOM_NOT_FOUND", "No rooms provided for booking");
        }

        for (com.hoanien.dto.RoomSelectionDTO selection : roomSelections) {
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
        java.util.List<com.hoanien.models.RoomCategory> categoriesToBook = new java.util.ArrayList<>();
        java.util.List<BigDecimal> roomCharges = new java.util.ArrayList<>();
        java.util.List<BigDecimal> extraSurcharges = new java.util.ArrayList<>();
        java.util.List<Integer> adultsList = new java.util.ArrayList<>();
        java.util.List<Integer> childrenList = new java.util.ArrayList<>();
        java.util.List<java.util.List<Integer>> childrenAgesList = new java.util.ArrayList<>();

        RoomBooking holdBooking = new RoomBooking();
        holdBooking.setCustomer(customer);
        holdBooking.setBookingDate(LocalDate.now());
        holdBooking.setTotalPrice(BigDecimal.ZERO);
        // Tạo thẳng Pending_Payment — giữ chỗ phòng ngay khi bấm Thanh toán
        holdBooking.setBookingStatus("Pending_Payment");
        holdBooking.setHoldExpiresAt(LocalDateTime.now().withNano(0).plusMinutes(2));
        holdBooking.setBookingSource("Direct_Web");
        holdBooking.setCheckInDate(checkIn);
        holdBooking.setCheckOutDate(checkOut);
        holdBooking.setDepositAmount(BigDecimal.ZERO);
        holdBooking.setCancellationDeadline(checkIn.atTime(14, 0).minusHours(48));
        holdBooking.setPersonalPinHash("HOLD_PENDING");
        holdBooking.setNotes(request.getNotes());

        BigDecimal maxTierLimit = new BigDecimal("5000000.00"); // Mặc định 5 triệu
        if (customer.getMembershipTier() != null && customer.getMembershipTier().getCreditLimit() != null) {
            maxTierLimit = customer.getMembershipTier().getCreditLimit();
        }

        BigDecimal creditLimit = (maxTierLimit != null && maxTierLimit.compareTo(BigDecimal.ZERO) > 0)
                ? maxTierLimit
                : new BigDecimal("5000000.00");

        holdBooking.setCreditLimit(creditLimit);

        RoomBooking savedHold = roomBookingRepository.save(holdBooking);
        roomBookingRepository.flush();

        log.info(" HOLD created: bookingId={}, customer={}, {} → {}",
                savedHold.getId(), customer.getId(), checkIn, checkOut);

        // Nhóm các phòng được chọn theo Hạng Phòng
        java.util.Map<String, java.util.List<com.hoanien.dto.RoomSelectionDTO>> groupedSelections = roomSelections
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(com.hoanien.dto.RoomSelectionDTO::getCategoryName));

        // Lặp qua từng Hạng Phòng để kiểm tra và tính tiền
        for (java.util.Map.Entry<String, java.util.List<com.hoanien.dto.RoomSelectionDTO>> entry : groupedSelections
                .entrySet()) {
            String catName = entry.getKey();
            java.util.List<com.hoanien.dto.RoomSelectionDTO> selections = entry.getValue();
            int requestedQty = selections.size();
            com.hoanien.models.RoomCategory category = roomCategoryRepository.findByCategoryNameWithLock(catName)
                    .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found: " + catName));

            // Tính toán số phòng không bị trùng
            long available = calculateAvailableRooms(catName, checkIn, checkOut);
            if (available < requestedQty) {
                throw new RoomNotAvailableException(
                        "Hạng phòng " + catName + " chỉ còn trống " + available + " phòng.");
            }
            BigDecimal baseTotal = pricingService.calculateTotalRoomCharge(category, checkIn, checkOut);
            BigDecimal pricePerNight = nights > 0
                    ? baseTotal.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            for (com.hoanien.dto.RoomSelectionDTO selection : selections) {
                // Tính phụ thu
                BigDecimal extraSurcharge = BigDecimal.ZERO;
                int reqAdults = selection.getNumberOfAdults() != null ? selection.getNumberOfAdults() : 0;
                int reqChildren = selection.getNumberOfChildren() != null ? selection.getNumberOfChildren() : 0;

                int maxAdults = category.getMaxAdults() != null ? category.getMaxAdults() : category.getCapacity();
                int maxChildren = category.getMaxChildren() != null ? category.getMaxChildren() : 2;

                if (reqAdults > maxAdults || reqChildren > maxChildren) {
                    throw new IllegalArgumentException(
                            "Số lượng khách vượt quá sức chứa tối đa của hạng phòng " + catName
                                    + ". Tối đa: " + maxAdults + " người lớn, " + maxChildren + " trẻ em.");
                }

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
                            .map(com.hoanien.models.RoomSurcharge::getPriceModifier)
                            .orElse(category.getExtraChildSurcharge() != null ? category.getExtraChildSurcharge()
                                    : BigDecimal.ZERO);
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
                roomCharges.add(pricePerNight);
                extraSurcharges.add(dailySurcharge);
                adultsList.add(reqAdults);
                childrenList.add(reqChildren);
                childrenAgesList.add(ages);
            }
        }

        BigDecimal discountedPrice = totalBaseTotal;
        String promoCode = request.getPromotionCode();
        if (promoCode != null && !promoCode.isBlank()) {
            discountedPrice = applyPromotion(promoCode, totalBaseTotal, request.getCustomerId());
            Promotion promo = promotionRepository.findByPromoCode(promoCode.trim().toUpperCase()).orElse(null);
            if (promo != null) {
                savedHold.setAppliedPromotion(promo);
            }
        }

        // Tính toán tiền đặt cọc ở backend (30% cọc mặc định)
        BigDecimal depositVal = discountedPrice.multiply(new BigDecimal("0.3")).setScale(0, RoundingMode.HALF_UP);
        // Cập nhật giá thực, giữ nguyên status "Pending"
        // Phòng chưa bị trừ — chỉ trừ khi confirmBooking() chuyển sang HOLD
        savedHold.setTotalPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        savedHold.setDepositAmount(depositVal);
        savedHold.setBookingStatus("Pending_Payment");
        savedHold.setHoldExpiresAt(LocalDateTime.now().withNano(0).plusMinutes(2));
        RoomBooking savedBooking = roomBookingRepository.save(savedHold);

        // Cập nhật thông tin khách hàng nếu chưa có trong profile (chỉ ghi khi trống)
        updateCustomerProfileIfBlank(customer, request);

        // Gọi Workflow Engine để kiểm tra nếu áp dụng mã giảm giá vượt ngưỡng
        if (promoCode != null && !promoCode.isBlank()) {
            try {
                Promotion promo = promotionRepository.findByPromoCode(promoCode).orElse(null);
                if (promo != null) {
                    BigDecimal pct = "Percentage".equalsIgnoreCase(promo.getDiscountType())
                            ? promo.getDiscountValue()
                            : (totalBaseTotal.compareTo(BigDecimal.ZERO) > 0
                                    ? promo.getDiscountValue().multiply(new BigDecimal("100")).divide(totalBaseTotal, 2,
                                            RoundingMode.HALF_UP)
                                    : BigDecimal.ZERO);

                    workflowEngineService.triggerEvent("PROMOTION_EXCEEDED", java.util.Map.of(
                            "promo_id", promo.getId(),
                            "input_discount_pct", pct.doubleValue(),
                            "booking_id", savedBooking.getId()));
                }
            } catch (Exception e) {
                log.error("Failed to trigger PROMOTION_EXCEEDED workflow in createBooking", e);
            }
        }

        log.info("HOLD updated with details: bookingId={}", savedBooking.getId());

        for (int i = 0; i < categoriesToBook.size(); i++) {
            com.hoanien.models.RoomCategory category = categoriesToBook.get(i);
            BigDecimal roomCharge = roomCharges.get(i);
            BigDecimal extraSurcharge = extraSurcharges.get(i);
            Integer reqAdults = adultsList.get(i);
            Integer reqChildren = childrenList.get(i);

            RoomBookingDetail detail = new RoomBookingDetail();
            detail.setRoomBooking(savedBooking);
            detail.setRoom(null);
            detail.setCategory(category);
            detail.setRoomCharge(roomCharge);
            detail.setExtraSurcharge(extraSurcharge);
            detail.setNumberOfAdults(reqAdults);
            detail.setNumberOfChildren(reqChildren);
            detail.setDetailStatus("Pending_Payment");
            detail.setCustomer(customer);

            // Mỗi phòng hưởng trọn hạn mức của hạng khách hàng đặt phòng (hoặc mặc định
            // 5.000.000đ)
            BigDecimal initialCreditLimit = (savedBooking.getCreditLimit() != null
                    && savedBooking.getCreditLimit().compareTo(BigDecimal.ZERO) > 0)
                            ? savedBooking.getCreditLimit()
                            : new BigDecimal("5000000.00");
            detail.setSubCreditLimit(initialCreditLimit);

            roomBookingDetailRepository.save(detail);

            // Create RoomGuest for Adults (Stub)
            for (int a = 0; a < reqAdults; a++) {
                com.hoanien.models.RoomGuest guest = new com.hoanien.models.RoomGuest();
                guest.setRoomBookingDetail(detail);
                guest.setGuestType("ADULT");
                if (a == 0 && i == 0) {
                    guest.setCustomer(customer);
                    guest.setIsPrimaryContact(true);
                } else {
                    com.hoanien.models.Dependent stubDep = new com.hoanien.models.Dependent();
                    stubDep.setCustomer(customer);
                    stubDep.setDependentName("Khách đi kèm");
                    stubDep.setBirthDate(java.time.LocalDate.now().minusYears(18).withDayOfYear(1));
                    stubDep.setGender("Khác");
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
                com.hoanien.models.Dependent dep = new com.hoanien.models.Dependent();
                dep.setCustomer(customer);
                dep.setDependentName("Khách đi kèm");
                dep.setGender("Khác");
                // Calculate approximate birthDate from age (e.g., Jan 1st of birth year)
                dep.setBirthDate(java.time.LocalDate.now().minusYears(age).withDayOfYear(1));
                dependentRepository.save(dep);

                com.hoanien.models.RoomGuest guest = new com.hoanien.models.RoomGuest();
                guest.setRoomBookingDetail(detail);
                guest.setGuestType("CHILD");
                guest.setDependent(dep);
                guest.setIsPrimaryContact(false);
                roomGuestRepository.save(guest);
            }
        }

        BookingResponseDTO response = new BookingResponseDTO();
        response.setBookingId(savedBooking.getId());
        response.setBookingStatus("Pending_Payment");
        response.setDepositAmount(depositVal);
        response.setDiscountedPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));
        response.setCheckInDate(checkIn);
        response.setCheckOutDate(checkOut);
        response.setCancellationDeadline(checkIn.atTime(14, 0).minusHours(48));

        log.info("createBooking → Pending_Payment: bookingId={}, customer={}, {}→{}, holdExpires={}",
                savedBooking.getId(), customer.getId(), checkIn, checkOut, savedBooking.getHoldExpiresAt());

        return response;
    }

    /**
     * Cập nhật các trường profile của Customer chỉ khi trường đó đang trống/null.
     * Tránh ghi đè dữ liệu đã có từ trước.
     */
    private void updateCustomerProfileIfBlank(Customer customer, BookingRequestDTO request) {
        boolean dirty = false;
        if (isBlank(customer.getFullName()) && !isBlank(request.getFullName())) {
            customer.setFullName(request.getFullName().trim());
            dirty = true;
        }
        if (isBlank(customer.getPhone()) && !isBlank(request.getPhone())) {
            customer.setPhone(com.hoanien.utils.ValidationUtils.normalizePhone(request.getPhone()));
            dirty = true;
        }
        if (isBlank(customer.getEmail()) && !isBlank(request.getEmail())) {
            customer.setEmail(request.getEmail().trim());
            dirty = true;
        }
        if (customer.getBirthDate() == null && !isBlank(request.getDateOfBirth())) {
            try {
                customer.setBirthDate(LocalDate.parse(request.getDateOfBirth()));
                dirty = true;
            } catch (Exception ignored) {
                log.warn("Invalid dateOfBirth format from request: {}", request.getDateOfBirth());
            }
        }
        if (dirty) {
            customerRepository.save(customer);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Scheduler chạy mỗi 60 giây, tìm các đơn Pending_Payment đã hết hạn
     * (holdExpiresAt ≤ now)
     * và xóa khỏi DB để giải phóng phòng.
     */
    @Transactional
    public void cleanupStaleHolds() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        List<RoomBooking> staleHolds = roomBookingRepository
                .findByBookingStatusInAndHoldExpiresAtBefore(java.util.List.of("Pending_Payment"), now);
        if (!staleHolds.isEmpty()) {
            staleHolds.forEach(h -> {
                try {
                    List<com.hoanien.models.RoomBookingDetail> details = roomBookingDetailRepository
                            .findByRoomBookingId(h.getId());
                    for (com.hoanien.models.RoomBookingDetail detail : details) {
                        if (detail.getRoom() != null) {
                            detail.getRoom().setRoomStatus("Vacant_Clean");
                            roomRepository.save(detail.getRoom());
                        }
                    }
                    roomBookingDetailRepository.deleteAll(details);
                    roomBookingRepository.delete(h);

                    if (h.getCustomer() != null) {
                        try {
                            notificationService.sendNotification(
                                    h.getCustomer().getId(),
                                    "Đơn đặt phòng tự động bị xóa",
                                    "Đơn đặt phòng #" + h.getId()
                                            + " của quý khách đã bị xóa do quá hạn 2 phút chờ thanh toán.");
                        } catch (Exception e) {
                            log.error("Failed to send deletion notification for booking {}", h.getId(), e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to delete stale booking {}", h.getId(), e);
                }
            });
            log.warn("Auto-deleted {} expired HOLD booking(s) at {}",
                    staleHolds.size(), now);
        }
    }

    /**
     * Áp dụng mã khuyến mãi (UC10.2).
     */
    private BigDecimal applyPromotion(String promoCode, BigDecimal baseTotal, Long customerId) {
        Promotion promo = promotionRepository.findByPromoCode(promoCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERR_PROMO_NOT_FOUND] Promotion code '" + promoCode + "' does not exist "));

        if (!Boolean.TRUE.equals(promo.getIsActive())) {
            throw new IllegalArgumentException(
                    "[ERR_PROMO_INACTIVE] Promotion code '" + promoCode + "' is invalid or expired ");
        }

        if (promo.getValidTo().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "[ERR_PROMO_EXPIRED] Promotion code '" + promoCode + "' has expired ");
        }

        if (promo.getMaxUses() != null && promo.getCurrentUses() != null
                && promo.getCurrentUses() >= promo.getMaxUses()) {
            throw new IllegalArgumentException(
                    "Mã giảm giá \"" + promoCode.trim().toUpperCase() + "\" đã hết lượt sử dụng.");
        }

        BigDecimal discountValue = promo.getDiscountValue();
        BigDecimal discountAmount;

        if (discountValue.compareTo(new BigDecimal("100")) <= 0) {
            discountAmount = baseTotal.multiply(discountValue)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = discountValue;
        }

        // Anti-Fraud check from Workflow Engine
        List<Workflow> activeWorkflows = workflowRepository.findByTriggerEventAndIsActive("PROMOTION_EXCEEDED", true);
        ObjectMapper mapper = new ObjectMapper();
        for (Workflow w : activeWorkflows) {
            try {
                if (w.getConditionsJson() != null && !w.getConditionsJson().trim().isEmpty()) {
                    Map<String, Object> conds = mapper.readValue(w.getConditionsJson(),
                            new TypeReference<Map<String, Object>>() {
                            });

                    // 1. max_discount_value_vnd
                    if (conds.containsKey("max_discount_value_vnd")) {
                        BigDecimal maxVal = new BigDecimal(conds.get("max_discount_value_vnd").toString());
                        if (discountAmount.compareTo(maxVal) > 0) {
                            throw new IllegalArgumentException("Mã giảm giá vượt quá hạn mức tối đa cho phép (" + maxVal
                                    + " VND) ");
                        }
                    }

                    // 2. max_uses_per_customer (Mặc định 1 lần duy nhất cho mỗi khách hàng)
                    if (customerId != null) {
                        int maxUses = conds.containsKey("max_uses_per_customer")
                                ? Integer.parseInt(conds.get("max_uses_per_customer").toString())
                                : 1;
                        long uses = bookingRepository.countByCustomerIdAndPromoCode(customerId,
                                promoCode.trim().toUpperCase());
                        if (uses >= maxUses) {
                            throw new IllegalArgumentException("Mã giảm giá \"" + promoCode.trim().toUpperCase()
                                    + "\" đã được sử dụng trước đó. Mỗi tài khoản chỉ được sử dụng 1 lần duy nhất.");
                        }
                    }
                    // 3. threshold_pct_gt (Chặn cứng đối với Khách hàng tự thao tác)
                    if (conds.containsKey("threshold_pct_gt")) {
                        BigDecimal thresholdVal = new BigDecimal(conds.get("threshold_pct_gt").toString());
                        BigDecimal pct = "Percentage".equalsIgnoreCase(promo.getDiscountType())
                                ? promo.getDiscountValue()
                                : (baseTotal.compareTo(BigDecimal.ZERO) > 0
                                        ? promo.getDiscountValue().multiply(new BigDecimal("100")).divide(baseTotal, 2,
                                                RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO);

                        if (pct.compareTo(thresholdVal) > 0) {
                            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                                    .getContext().getAuthentication();
                            boolean isStaff = auth != null && auth.getAuthorities().stream().anyMatch(a -> {
                                String r = a.getAuthority();
                                return r.equals("ROLE_ADMIN") || r.equals("ROLE_MANAGER")
                                        || r.equals("ROLE_RECEPTIONIST") || r.equals("ROLE_STAFF");
                            });

                            // if (!isStaff) {
                            // throw new IllegalArgumentException(
                            // "Mã giảm giá vượt quá mức cho phép đối với khách tự đặt ("
                            // + thresholdVal
                            // + "%). Vui lòng liên hệ Lễ tân để được hỗ trợ đền bù.");
                            // }
                            // Nếu là Staff -> Cho qua để hệ thống bắt vào luồng Workflow Treo chờ duyệt.
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException)
                    throw (IllegalArgumentException) e;
                log.error("Failed executing anti-fraud conditions evaluation", e);
            }
        }

        BigDecimal finalPrice = baseTotal.subtract(discountAmount);
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
    @Transactional(rollbackFor = Exception.class)
    public BookingResponseDTO cancelBooking(Long bookingId, Long customerId,
            com.hoanien.dto.CancelBookingRequestDTO dto) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN",
                        "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        String status = booking.getBookingStatus() != null ? booking.getBookingStatus().toUpperCase() : "";
        if ("CHECKED_IN".equalsIgnoreCase(status)
                || "CANCELLED".equalsIgnoreCase(status)
                || status.startsWith("CANCEL")) {
            throw new BusinessException("BKG-005", "Invalid booking status");
        }

        if ("Pending_Payment".equalsIgnoreCase(booking.getBookingStatus())) {
            deletePendingBooking(bookingId, customerId);
            BookingResponseDTO response = new BookingResponseDTO();
            response.setBookingId(bookingId);
            response.setBookingStatus("CANCELLED");
            response.setDepositAmount(BigDecimal.ZERO);
            return response;
        }

        // Nếu booking chưa đóng tiền cọc (depositAmount = null hoặc = 0)
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
        java.time.LocalDateTime now = java.time.LocalDateTime.now().withNano(0);
        boolean isEligibleForRefund = booking.getCancellationDeadline() != null
                && !now.isAfter(booking.getCancellationDeadline());

        try {
            java.math.BigDecimal totalRefundAmount = isEligibleForRefund ? booking.getDepositAmount()
                    : java.math.BigDecimal.ZERO;

            if (isEligibleForRefund) {
                if (dto != null) {
                    com.hoanien.models.RefundRequest refund = new com.hoanien.models.RefundRequest();
                    refund.setRoomBooking(booking);
                    refund.setBankName(dto.getBankName());
                    refund.setAccountNumber(dto.getAccountNumber());
                    refund.setAccountName(dto.getAccountName());
                    refund.setPhoneNumber(dto.getPhoneNumber());
                    refund.setAmount(booking.getDepositAmount());
                    refund.setStatus("Pending");
                    refundRequestRepository.save(refund);
                }

                if (notificationService != null) {
                    notificationService.sendNotification(customerId, "Cancel Success",
                            "Your booking has been cancelled. A refund request has been created and will be processed shortly.");
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

            // Xử lý hủy các Tour đi kèm
            java.math.BigDecimal totalTourRefund = processAttachedToursCancellation(bookingId, dto);
            totalRefundAmount = totalRefundAmount.add(totalTourRefund);

            try {
                emailService.sendRoomCancellationEmail(booking, booking.getCustomer(), isEligibleForRefund);
            } catch (Exception e) {
                log.error("Lỗi gửi email xác nhận hủy đặt phòng: ", e);
            }

            BookingResponseDTO response = new BookingResponseDTO();
            response.setBookingId(bookingId);
            response.setBookingStatus(newStatus);
            response.setDepositAmount(totalRefundAmount);
            return response;
        } catch (Exception e) {
            if (notificationService != null) {
                notificationService.sendNotification(customerId, "Cancel Failed",
                        "Cancellation failed due to a system error. Please try again later.");
            }
            throw e;
        }
    }

    private java.math.BigDecimal processAttachedToursCancellation(Long bookingId,
            com.hoanien.dto.CancelBookingRequestDTO dto) {
        java.math.BigDecimal totalTourRefund = java.math.BigDecimal.ZERO;
        java.util.List<com.hoanien.models.TourBooking> attachedTours = tourBookingRepository
                .findByRoomBookingId(bookingId);

        for (com.hoanien.models.TourBooking tb : attachedTours) {
            String tbStatus = tb.getBookingStatus() != null ? tb.getBookingStatus().toUpperCase() : "";
            if ("PENDING".equals(tbStatus) || "PENDING_PAYMENT".equals(tbStatus) || "CONFIRMED".equals(tbStatus)) {
                boolean isTourRefundable = false;
                if (tb.getSchedule() != null && tb.getSchedule().getDepartureDate() != null) {
                    java.time.LocalDateTime depTime = tb.getSchedule().getDepartureDate().atTime(
                            tb.getSchedule().getDepartureTime() != null ? tb.getSchedule().getDepartureTime()
                                    : java.time.LocalTime.of(7, 0));
                    if (java.time.temporal.ChronoUnit.HOURS.between(java.time.LocalDateTime.now().withNano(0), depTime) > 24) {
                        isTourRefundable = true;
                    }
                }

                String newTourStatus = isTourRefundable ? "Cancelled_Refunded" : "Cancelled_Forfeited";
                tb.setBookingStatus(newTourStatus);
                tourBookingRepository.save(tb);

                if (isTourRefundable) {
                    java.math.BigDecimal tourRefund = tb.getTourCharge() != null
                            ? tb.getTourCharge().multiply(new java.math.BigDecimal("0.5"))
                            : java.math.BigDecimal.ZERO;
                    totalTourRefund = totalTourRefund.add(tourRefund);

                    if (dto != null && tourRefund.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        com.hoanien.models.RefundRequest tourRefundReq = new com.hoanien.models.RefundRequest();
                        tourRefundReq.setTourBooking(tb);
                        tourRefundReq.setBankName(dto.getBankName());
                        tourRefundReq.setAccountNumber(dto.getAccountNumber());
                        tourRefundReq.setAccountName(dto.getAccountName());
                        tourRefundReq.setPhoneNumber(dto.getPhoneNumber());
                        tourRefundReq.setAmount(tourRefund);
                        tourRefundReq.setStatus("Pending");
                        refundRequestRepository.save(tourRefundReq);
                    }
                }
            }
        }
        return totalTourRefund;
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
        // SoftLock countdown: tính số giây còn lại khi đơn đang chờ thanh toán VNPay
        if ("Pending_Payment".equals(booking.getBookingStatus()) && booking.getHoldExpiresAt() != null) {
            remainingSeconds = java.time.temporal.ChronoUnit.SECONDS.between(LocalDateTime.now().withNano(0),
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
        com.hoanien.models.Booking generalBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN",
                        "Đơn đặt dịch vụ không thuộc về tài khoản này hoặc không tồn tại!"));

        if (generalBooking.getCustomer() == null || !generalBooking.getCustomer().getId().equals(customerId)) {
            throw new BusinessException("FORBIDDEN",
                    "Đơn đặt dịch vụ không thuộc về tài khoản này hoặc không tồn tại!");
        }

        BigDecimal totalBaseTotal = BigDecimal.ZERO;

        if (generalBooking instanceof RoomBooking) {
            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
            BigDecimal baseRoomPrice = BigDecimal.ZERO;
            BigDecimal servicesFee = BigDecimal.ZERO;
            for (RoomBookingDetail detail : details) {
                baseRoomPrice = baseRoomPrice
                        .add(detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO);
                servicesFee = servicesFee
                        .add(detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO);
            }
            totalBaseTotal = baseRoomPrice.add(servicesFee);
        } else if (generalBooking instanceof com.hoanien.models.TourBooking) {
            com.hoanien.models.TourBooking booking = (com.hoanien.models.TourBooking) generalBooking;
            totalBaseTotal = booking.getTourCharge() != null ? booking.getTourCharge() : booking.getTotalPrice();
            if (totalBaseTotal == null) {
                totalBaseTotal = BigDecimal.ZERO;
            }
        } else {
            throw new BusinessException("NOT_SUPPORTED", "Loại đơn hàng này không hỗ trợ áp dụng mã giảm giá!");
        }

        BigDecimal discountedPrice = applyPromotion(couponCode, totalBaseTotal, customerId);
        BigDecimal discountAmount = totalBaseTotal.subtract(discountedPrice);

        generalBooking.setTotalPrice(discountedPrice.setScale(0, RoundingMode.HALF_UP));

        Promotion promotion = promotionRepository.findByPromoCode(couponCode)
                .orElseThrow(() -> new BusinessException("PROMOTION_NOT_FOUND",
                        "Mã giảm giá không tồn tại hoặc đã hết hạn!"));
        generalBooking.setAppliedPromotion(promotion);

        if (generalBooking instanceof RoomBooking) {
            RoomBooking roomBooking = (RoomBooking) generalBooking;
            roomBooking.setDepositAmount(
                    discountedPrice.multiply(new BigDecimal("0.3")).setScale(0, RoundingMode.HALF_UP));
            roomBookingRepository.save(roomBooking);
        } else if (generalBooking instanceof com.hoanien.models.TourBooking) {
            tourBookingRepository.save((com.hoanien.models.TourBooking) generalBooking);
        }

        return discountAmount.setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void confirmBooking(Long bookingId, Long customerId, String fullName, String phone, String email,
            String cccd, java.time.LocalDate dateOfBirth, String address, String notes, String paymentMethod) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("FORBIDDEN",
                        "Đơn đặt phòng không thuộc về tài khoản này hoặc không tồn tại!"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Không tìm thấy thông tin khách hàng!"));

        if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
            java.util.List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
            java.util.Map<String, Long> categoryCountMap = details.stream()
                    .collect(java.util.stream.Collectors.groupingBy(d -> d.getCategory().getCategoryName(),
                            java.util.stream.Collectors.counting()));

            for (java.util.Map.Entry<String, Long> entry : categoryCountMap.entrySet()) {
                String catName = entry.getKey();
                long requestedQty = entry.getValue();
                // Persimistic_Lock
                roomCategoryRepository.findByCategoryNameWithLock(catName)
                        .orElseThrow(
                                () -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found: " + catName));

                long available = calculateAvailableRooms(catName, booking.getCheckInDate(), booking.getCheckOutDate());
                if (available < requestedQty) {
                    throw new BusinessException("ROOM_UNAVAILABLE",
                            "Rất tiếc, hạng phòng " + catName + " đã hết phòng trống. Vui lòng chọn lại!");
                }
            }
            // Set status based on payment method
            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                booking.setBookingStatus("Pending_Payment");
                booking.setHoldExpiresAt(LocalDateTime.now().withNano(0).plusMinutes(2));
                log.info(" Pending→Pending_Payment: bookingId={}, holdExpiresAt={}",
                        bookingId, booking.getHoldExpiresAt());
            } else {
                booking.setBookingStatus("Confirmed");
                booking.setHoldExpiresAt(null);
                log.info("Pending->Confirmed: bookingId={}", bookingId);
                triggerBookingCreatedWorkflow(booking, customer);
            }
        } else if ("Pending_Payment".equalsIgnoreCase(booking.getBookingStatus())) {
            // User thử lại VNPay (ví dụ back lại trang payment) → refresh timer
            if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                booking.setHoldExpiresAt(LocalDateTime.now().withNano(0).plusMinutes(2));
                log.info(" Pending_Payment refreshed: bookingId={}, holdExpiresAt={}",
                        bookingId, booking.getHoldExpiresAt());
            } else {
                booking.setBookingStatus("Confirmed");
                booking.setHoldExpiresAt(null);
                triggerBookingCreatedWorkflow(booking, customer);
            }
        }

        // Cập nhật thông tin khách hàng từ form NẾU họ chưa có thông tin trong profile
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            customer.setFullName(fullName);
        }

        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            if (!com.hoanien.utils.ValidationUtils.isValidPhone(phone)) {
                throw new BusinessException("INVALID_PHONE",
                        "Số điện thoại không hợp lệ (Phải gồm 10 số và bắt đầu bằng 0)");
            }
            customer.setPhone(com.hoanien.utils.ValidationUtils.normalizePhone(phone));
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            customer.setEmail(email);
        }

        // Kiểm tra đủ 18 tuổi nếu có dateOfBirth
        if (dateOfBirth != null) {
            int age = java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
            if (age < 18) {
                throw new BusinessException("AGE_RESTRICTION", "Bạn phải từ đủ 18 tuổi trở lên để đặt phòng.");
            }
            // Chỉ cập nhật nếu profile chưa có
            if (customer.getBirthDate() == null) {
                customer.setBirthDate(dateOfBirth);
            }
        }

        if (cccd != null && !cccd.equals("********") && !cccd.trim().isEmpty()) {
            if (!com.hoanien.utils.ValidationUtils.isValidDocument(cccd)) {
                throw new BusinessException("INVALID_CCCD",
                        "CCCD/Passport không hợp lệ (Phải là CCCD 12 số, hoặc Passport 8-12 ký tự có chứa chữ cái)");
            }
            // Chỉ cập nhật nếu profile chưa có cccd
            if (customer.getCccdPassportEncrypted() == null || customer.getCccdPassportEncrypted().isEmpty()) {
                customer.setCccdPassportEncrypted(com.hoanien.utils.EncryptionUtils.encrypt(cccd.trim()));
            }
        }

        if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            if (address != null && !address.trim().isEmpty()) {
                customer.setAddress(address.trim());
            }
        }
        customerRepository.save(customer);
        booking.setNotes(notes);
        roomBookingRepository.save(booking);
    }

    private void triggerBookingCreatedWorkflow(RoomBooking booking, Customer customer) {
        try {
            long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(),
                    booking.getCheckOutDate());
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("booking_id", booking.getId());
            payload.put("stay_nights", nights);
            payload.put("customer_email", customer.getEmail());
            payload.put("customer_name", customer.getFullName());
            workflowEngineService.triggerEvent("BOOKING_CREATED", payload);
        } catch (Exception e) {
            log.error("Failed to trigger BOOKING_CREATED workflow", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getBookingFolios(Long bookingId, Long customerId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Không tìm thấy đơn đặt phòng!"));

        boolean isMasterBooker = booking.getCustomer() != null && booking.getCustomer().getId().equals(customerId);

        List<com.hoanien.models.RoomBookingDetail> visibleDetails = new java.util.ArrayList<>();

        if (isMasterBooker) {
            visibleDetails = roomBookingDetailRepository.findByRoomBookingId(bookingId);
        } else {
            // Check if primary contact
            List<com.hoanien.models.RoomGuest> guests = roomGuestRepository.findByCustomerId(customerId);
            for (com.hoanien.models.RoomGuest g : guests) {
                if (Boolean.TRUE.equals(g.getIsPrimaryContact()) &&
                        g.getRoomBookingDetail() != null &&
                        g.getRoomBookingDetail().getRoomBooking() != null &&
                        g.getRoomBookingDetail().getRoomBooking().getId().equals(bookingId)) {
                    visibleDetails.add(g.getRoomBookingDetail());
                }
            }
            if (visibleDetails.isEmpty()) {
                throw new BusinessException("FORBIDDEN",
                        "Bạn không có quyền xem thông tin chi phí của đơn đặt phòng này!");
            }
        }

        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.hoanien.models.RoomBookingDetail detail : visibleDetails) {
            List<com.hoanien.models.FolioItem> folios = folioItemRepository.findByRoomBookingDetailId(detail.getId());
            for (com.hoanien.models.FolioItem f : folios) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("folioItemId", f.getId());
                map.put("roomBookingDetailId", detail.getId());
                map.put("roomNumber", detail.getRoom() != null ? detail.getRoom().getRoomNumber() : null);
                map.put("payerCustomerId", f.getPayerCustomer() != null ? f.getPayerCustomer().getId() : null);
                map.put("payerName", f.getPayerCustomer() != null ? f.getPayerCustomer().getFullName() : null);
                map.put("sourceDepartment", f.getSourceDepartment());
                map.put("amount", f.getAmount());
                map.put("description", f.getDescription());
                map.put("isSettledSeparately", f.getIsSettledSeparately());
                map.put("createdAt", f.getCreatedAt());
                result.add(map);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void deletePendingBooking(Long bookingId, Long customerId) {
        RoomBooking booking = roomBookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BusinessException("BOOKING_NOT_FOUND", "Không tìm thấy đơn đặt phòng"));

        if (!"Pending_Payment".equalsIgnoreCase(booking.getBookingStatus())) {
            throw new BusinessException("INVALID_STATE", "Chỉ có thể xóa đơn đang ở trạng thái chờ");
        }

        // Thay vì xóa cứng, ta chuyển trạng thái sang Cancelled_Payment để giải phóng
        // phòng
        booking.setBookingStatus("Cancelled_Payment");
        booking.setHoldExpiresAt(null);

        List<com.hoanien.models.RoomBookingDetail> details = roomBookingDetailRepository
                .findByRoomBookingId(booking.getId());
        for (com.hoanien.models.RoomBookingDetail detail : details) {
            detail.setDetailStatus("Cancelled_Payment");
            roomBookingDetailRepository.save(detail);
        }

        roomBookingRepository.save(booking);
        log.info("Soft-cancelled pending booking {} manually by customer {}", bookingId, customerId);
    }

    private long calculateAvailableRooms(String catName, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        boolean isTodayOrPast = !checkIn.isAfter(java.time.LocalDate.now());
        java.util.List<com.hoanien.models.Room> roomsInCat = roomRepository.findByCategoryName(catName);

        if (isTodayOrPast) {
            long physicallyAvailableToday = roomsInCat.stream()
                    .filter(r -> !"Occupied".equalsIgnoreCase(r.getRoomStatus())
                            && !"Maintenance".equalsIgnoreCase(r.getRoomStatus()))
                    .count();

            long overlappingNotCheckedIn = roomBookingRepository.countOverlappingNotCheckedIn(catName, checkIn,
                    checkOut);

            return physicallyAvailableToday - overlappingNotCheckedIn;
        } else {
            long physicallyAvailableFuture = roomsInCat.stream()
                    .filter(r -> !"Maintenance".equalsIgnoreCase(r.getRoomStatus()))
                    .count();
            long overlappingBookings = roomBookingRepository.countOverlappingBookingsByCategoryWithoutExclude(catName,
                    checkIn, checkOut);
            return Math.min(roomsInCat.size() - overlappingBookings, physicallyAvailableFuture);
        }

    }
}