package com.kawai.services.impl;

import com.kawai.dto.DependentRegistrationDTO;

import com.kawai.dto.walkin.WalkInCheckInRequest;
import com.kawai.dto.walkin.WalkInCheckInResponse;
import com.kawai.dto.walkin.WalkInRoomSelectionDTO;
import com.kawai.dto.walkin.WalkInSurchargeResponse;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.WalkInCheckInService;
import com.kawai.utils.EncryptionUtils;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WalkInCheckInServiceImpl — UC-14: Walk-in Guest Check-in
 * MODULE 2: Đặt phòng & Tiền sảnh vận hành
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * CHANGELOG:
 * 2026-06-30 | Antigravity AI | REFACTOR (Clean Code): đặt tên constants
 * | | (ROOM_STATUS_*, BOOKING_STATUS_*, GUEST_TYPE_*,
 * | | DEFAULT_CREDIT_LIMIT), loại bỏ double-loop
 * | | pre-validation, tách private methods
 * | | (buildMasterRoomGuest, buildAndSaveDependentGuest,
 * | | validatePrimaryContactCount, applyDepositToFolio,
 * | | buildWalkInResponse), thay array trick bằng
 * | | CustomerLookupResult record-like class.
 * 2026-06-22 | Chu Xuân Dũng | GREEN: implement WalkInCheckInServiceImpl, pass
 * 12 TC.
 * 2026-06-19 | Chu Xuân Dũng | Khởi tạo skeleton theo TDD_UC14_SPEC.md.
 *
 * Business Rules:
 * BR-UC14-01 : CCCD bắt buộc đúng format 12 chữ số (nếu được cung cấp)
 * BR-UC14-02 : Phòng phải Vacant_Clean
 * BR-UC14-03 : Booking được tạo trong 1 @Transactional
 * BR-UC14-05 : Booking status → CHECKED_IN
 * BR-UC14-08 : Tự động tạo Account cho khách mới
 * BR-UC14-09 : Default password được gán
 * BR-UC14-10 : Account phải link với Reservation
 *
 * Capacity Rules (Soft/Hard Limit):
 * Người lớn : >= 18 tuổi
 * Trẻ em : < 18 tuổi
 * Soft Limit: adults > baseAdults hoặc children > baseChildren → tính extra
 * surcharge
 * Hard Limit: adults > maxAdults hoặc children > maxChildren → reject
 * (MOD2-UC14-009)
 */
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WalkInCheckInServiceImpl implements com.kawai.services.interfaces.WalkInCheckInService {

    // ── Business Constants ───────────────────────────────────────────────────
    private static final int ADULT_AGE_THRESHOLD = 18;
    private static final String CCCD_PATTERN = "\\d{12}";
    private static final BigDecimal DEFAULT_CREDIT_LIMIT = new BigDecimal("5000000.00");
    private static final String DEFAULT_GUEST_NAME = "Khách lưu trú";
    private static final String DEFAULT_GENDER = "Khác";
    private static final String DEFAULT_PAYMENT_METHOD = "Tiền mặt";

    // Room status constants
    private static final String ROOM_STATUS_VACANT_CLEAN = "Vacant_Clean";
    private static final String ROOM_STATUS_VACANT_DIRTY = "Vacant_Dirty";
    private static final String ROOM_STATUS_OCCUPIED = "Occupied";

    // Booking & detail status constants
    private static final String BOOKING_STATUS_CHECKED_IN = "Checked_In";
    private static final String BOOKING_STATUS_PENDING_PAYMENT = "Pending_Payment";
    private static final String BOOKING_STATUS_CANCELLED = "Cancelled";
    private static final String BOOKING_SOURCE_WALK_IN = "WALK_IN";

    // Guest type constants
    private static final String GUEST_TYPE_ADULT = "ADULT";
    private static final String GUEST_TYPE_CHILD = "CHILD";

    // ── Dependencies ─────────────────────────────────────────────────────────
    private final RoomRepository roomRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final DependentRepository dependentRepository;
    private final RoomSurchargeRepository roomSurchargeRepository;
    private final RoomGuestRepository roomGuestRepository;
    private final RoleRepository roleRepository;
    private final MembershipTierRepository membershipTierRepository;
    private final com.kawai.services.interfaces.FolioService folioService;
    private final PasswordEncoder passwordEncoder;
    private final com.kawai.services.interfaces.CheckinService checkinService;
    private final com.kawai.repositories.MaintenanceRequestRepository maintenanceRequestRepo;
    private final com.kawai.services.interfaces.EmailService emailService;

    public WalkInCheckInServiceImpl(
            RoomRepository roomRepository,
            RoomBookingRepository roomBookingRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            DependentRepository dependentRepository,
            RoomSurchargeRepository roomSurchargeRepository,
            RoomGuestRepository roomGuestRepository,
            RoleRepository roleRepository,
            MembershipTierRepository membershipTierRepository,
            com.kawai.services.interfaces.FolioService folioService,
            PasswordEncoder passwordEncoder,
            com.kawai.repositories.MaintenanceRequestRepository maintenanceRequestRepo,
            @Lazy com.kawai.services.interfaces.CheckinService checkinService,
            com.kawai.services.interfaces.EmailService emailService) {
        this.roomRepository = roomRepository;
        this.roomBookingRepository = roomBookingRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.dependentRepository = dependentRepository;
        this.roomSurchargeRepository = roomSurchargeRepository;
        this.roomGuestRepository = roomGuestRepository;
        this.roleRepository = roleRepository;
        this.membershipTierRepository = membershipTierRepository;
        this.folioService = folioService;
        this.passwordEncoder = passwordEncoder;
        this.maintenanceRequestRepo = maintenanceRequestRepo;
        this.checkinService = checkinService;
        this.emailService = emailService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UC-14 — createWalkInBookingAndCheckIn()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Thực hiện toàn bộ luồng Walk-in Check-in trong 1 ACID Transaction.
     * ADR-UC14-003: Toàn bộ walk-in flow là 1 @Transactional.
     *
     * Flow tổng quát:
     * 1. Validate thông tin định danh (fail-fast)
     * 2. Tính tổng credit limit được phân bổ từ request
     * 3. Find-or-Create Customer
     * 4. Auto-create Account nếu khách mới (BR-08/09)
     * 5. Validate tổng credit limit vs membership tier
     * 6. Tạo RoomBooking master
     * 7. Với mỗi phòng: lock + validate → tạo detail → gán guest → phụ thu
     * 8. Cập nhật tổng giá booking
     * 9. Build & return response
     */
    @Override
    @Transactional
    public WalkInCheckInResponse createWalkInBookingAndCheckIn(WalkInCheckInRequest request) {
        try {
            // Step 1: Validate thông tin định danh (fail-fast trước DB)
            validateIdentification(request);

            // Step 2: Pre-validate TẤT CẢ phòng ngay từ đầu (fail-fast trước
            // Customer/Booking creation).
            // (TC-M2-030: phòng DIRTY/MAINTENANCE phải reject trước khi bất kỳ entity nào
            // được save)
            // (TC-M2-035: hard capacity violation phải reject trước khi Booking được save)
            BigDecimal totalAllocatedCreditLimit = sumAllocatedCreditLimit(request);
            java.util.Map<Long, Room> validatedRooms = new java.util.LinkedHashMap<>();
            boolean isFirstPreCheck = true;
            for (WalkInRoomSelectionDTO preCheck : request.getRoomSelections()) {
                Room validRoom = findAndValidateRoom(preCheck.getRoomId()); // throws nếu phòng không hợp lệ
                // Validate hard capacity trước khi lưu bất kỳ entity nào
                List<DependentRegistrationDTO> preCompanions = resolveCompanions(preCheck);
                List<Integer> preChildAges = new java.util.ArrayList<>();
                LocalDate prePrimaryDob = isFirstPreCheck ? request.getDateOfBirth() : null;
                GuestCount preGuestCount = classifyGuests(prePrimaryDob, preCompanions, preChildAges);
                validateAndCalculateSurcharge(preGuestCount, validRoom.getCategory(), preChildAges);
                validatedRooms.put(preCheck.getRoomId(), validRoom);
                isFirstPreCheck = false;
            }

            // Steps 3–4: Customer & Account (chỉ chạy nếu tất cả phòng đã valid)
            CustomerLookupResult customerResult = findOrCreateCustomerWithFlag(request);
            Customer customer = customerResult.customer;

            Account newAccount = null;
            String rawPassword = null;
            if (customerResult.isNew) {
                rawPassword = java.util.UUID.randomUUID().toString().substring(0, 6);
                newAccount = autoCreateAccount(customer, request.getEmail(), rawPassword);
            }

            // Step 5: Validate tổng credit limit vs membership tier
            validateCreditLimit(customer, totalAllocatedCreditLimit);

            // Step 6: Tạo RoomBooking master
            RoomBooking booking = roomBookingRepository.save(buildRoomBooking(request, customer));
            BigDecimal totalDeposit = resolveDepositAmount(request);
            booking.setDepositAmount(totalDeposit);

            // Step 7: Xử lý từng phòng (dùng cache room đã validate — không gọi repository
            // lại)
            BigDecimal bookingTotalPrice = BigDecimal.ZERO;
            boolean isFirstRoom = true;
            String firstRoomNumber = "";

            for (WalkInRoomSelectionDTO selection : request.getRoomSelections()) {
                Room room = validatedRooms.get(selection.getRoomId()); // reuse từ cache
                if (isFirstRoom)
                    firstRoomNumber = room.getRoomNumber();

                List<DependentRegistrationDTO> companions = resolveCompanions(selection);
                List<Integer> childAges = new java.util.ArrayList<>();
                LocalDate primaryDob = isFirstRoom ? request.getDateOfBirth() : null;

                GuestCount guestCount = classifyGuests(primaryDob, companions, childAges);
                BigDecimal extraSurcharge = validateAndCalculateSurcharge(guestCount, room.getCategory(), childAges);

                RoomBookingDetail detail = buildRoomBookingDetail(request, booking, room, room.getCategory(),
                        guestCount, extraSurcharge);
                detail.setSubCreditLimit(
                        selection.getAllocatedCreditLimit() != null ? selection.getAllocatedCreditLimit()
                                : BigDecimal.ZERO);
                roomBookingDetailRepository.save(detail);

                bookingTotalPrice = bookingTotalPrice.add(calculateDetailCharge(booking, detail));

                room.setRoomStatus(ROOM_STATUS_OCCUPIED);
                roomRepository.save(room);

                // Lưu RoomGuest: Master Customer (phòng đầu tiên)
                if (isFirstRoom) {
                    buildMasterRoomGuest(detail, customer);
                }

                // Lưu RoomGuest: Dependents đi kèm
                for (DependentRegistrationDTO dto : companions) {
                    boolean isPrimary = !isFirstRoom && Boolean.TRUE.equals(dto.getIsPrimaryContact());
                    buildAndSaveDependentGuest(detail, customer, dto, isPrimary);
                }

                validatePrimaryContactCount(room, companions, isFirstRoom);
                applyDepositToFolioIfNeeded(isFirstRoom, totalDeposit, request, detail);

                isFirstRoom = false;
            }

            // Step 8: Cập nhật tổng giá booking
            booking.setTotalPrice(bookingTotalPrice);
            roomBookingRepository.save(booking);

            // Step 9: Gửi email xác nhận
            if (BOOKING_STATUS_CHECKED_IN.equals(booking.getBookingStatus())) {
                try {
                    RoomBookingDetail firstDetail = roomBookingDetailRepository.findByRoomBookingId(booking.getId()).stream().findFirst().orElse(null);
                    if (firstDetail != null) {
                        String userParam = newAccount != null ? newAccount.getUsername() : null;
                        emailService.sendWalkInCheckInEmail(booking, firstDetail, customer, customerResult.isNew, userParam, rawPassword);
                    }
                } catch (Exception e) {
                    log.error("Lỗi gửi email Walk-in Check-in: ", e);
                }
            }

            return buildWalkInResponse(booking, firstRoomNumber, request, customer, customerResult.isNew, newAccount);

        } catch (BusinessException ex) {
            throw ex; // Re-throw đã có errorCode — không bọc thêm
        } catch (Exception ex) {
            throw new BusinessException("MOD2-UC14-005",
                    "Walk-in check-in failed. Transaction rolled back: " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // cancelPendingWalkIn()
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void cancelPendingWalkIn(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("MOD2-UC14-012", "Booking not found"));

        if (!BOOKING_SOURCE_WALK_IN.equals(booking.getBookingSource())
                || !BOOKING_STATUS_PENDING_PAYMENT.equals(booking.getBookingStatus())) {
            throw new BusinessException("MOD2-UC14-013",
                    "Only Walk-in bookings with Pending_Payment can be cancelled via this API");
        }

        booking.setBookingStatus(BOOKING_STATUS_CANCELLED);
        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
        for (RoomBookingDetail detail : details) {
            detail.setDetailStatus(BOOKING_STATUS_CANCELLED);
            if (detail.getRoom() != null) {
                detail.getRoom().setRoomStatus(ROOM_STATUS_VACANT_CLEAN);
                roomRepository.save(detail.getRoom());
            }
        }
        roomBookingDetailRepository.saveAll(details);
        roomBookingRepository.save(booking);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // calculateSurchargePreview()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Tính toán số khách (dựa trên tuổi) và số tiền phụ thu dự kiến.
     * API này được gọi riêng biệt bởi frontend để preview giá trước khi check-in.
     */
    @Override
    public WalkInSurchargeResponse calculateSurchargePreview(WalkInCheckInRequest request) {
        if (request.getRoomSelections() == null || request.getRoomSelections().isEmpty()) {
            throw new BusinessException("MOD2-UC14-015", "Không có phòng nào được chọn.");
        }

        BigDecimal totalExtraSurcharge = BigDecimal.ZERO;
        BigDecimal totalBasePricePerNight = BigDecimal.ZERO;
        int totalAdults = 0;
        int totalChildren = 0;
        boolean isFirstRoom = true;

        for (WalkInRoomSelectionDTO selection : request.getRoomSelections()) {
            Room room = roomRepository.findById(selection.getRoomId())
                    .orElseThrow(() -> new BusinessException("MOD2-UC14-004",
                            "Room not found for ID: " + selection.getRoomId()));
            RoomCategory category = room.getCategory();
            totalBasePricePerNight = totalBasePricePerNight
                    .add(category.getBasePrice() != null ? category.getBasePrice() : BigDecimal.ZERO);

            List<DependentRegistrationDTO> companions = resolveCompanions(selection);
            List<Integer> childAges = new java.util.ArrayList<>();
            LocalDate primaryDob = isFirstRoom ? request.getDateOfBirth() : null;
            isFirstRoom = false;

            GuestCount guestCount = classifyGuests(primaryDob, companions, childAges);
            totalAdults += guestCount.adults;
            totalChildren += guestCount.children;

            try {
                totalExtraSurcharge = totalExtraSurcharge
                        .add(validateAndCalculateSurcharge(guestCount, category, childAges));
            } catch (BusinessException ex) {
                throw new BusinessException(ex.getErrorCode(),
                        "Phòng " + room.getRoomNumber() + ": " + ex.getMessage());
            }
        }

        long nights = resolveNights(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalBaseRoomPrice = totalBasePricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal totalSurchargeAllNights = totalExtraSurcharge.multiply(BigDecimal.valueOf(nights));
        BigDecimal totalCharge = totalBaseRoomPrice.add(totalSurchargeAllNights);

        String msg = totalExtraSurcharge.compareTo(BigDecimal.ZERO) == 0
                ? "Miễn phí phụ thu (số khách nằm trong sức chứa tiêu chuẩn)"
                : "Phụ thu dự tính: " + totalExtraSurcharge + "/đêm";

        BigDecimal suggestedDeposit = totalCharge.multiply(new BigDecimal("0.3"))
                .setScale(0, java.math.RoundingMode.HALF_UP);

        return new WalkInSurchargeResponse(totalSurchargeAllNights, totalAdults, totalChildren, msg,
                totalBaseRoomPrice, totalCharge, suggestedDeposit);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // searchCustomer()
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public java.util.Optional<Customer> searchCustomer(String keyword) {
        if (keyword == null || keyword.isBlank())
            return java.util.Optional.empty();

        java.util.Optional<Customer> byPhone = customerRepository.findFirstByPhone(keyword);
        if (byPhone.isPresent())
            return byPhone;

        try {
            return customerRepository.findFirstByCccdPassportEncrypted(EncryptionUtils.encrypt(keyword));
        } catch (Throwable e) {
            return java.util.Optional.empty();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Validation Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Validate thông tin định danh: dateOfBirth bắt buộc; CCCD đúng format 12 số
     * nếu có.
     */
    private void validateIdentification(WalkInCheckInRequest req) {
        if (req.getDateOfBirth() == null) {
            throw new BusinessException("MOD2-UC14-001", "Date of birth is required");
        }
        String cccd = req.getCccd();
        if (cccd != null && !cccd.isBlank() && !cccd.matches(CCCD_PATTERN)) {
            throw new BusinessException("MOD2-UC14-003",
                    "Invalid identification document: CCCD must be 12 digits");
        }
    }

    /**
     * Validate tổng credit limit được phân bổ không vượt quá hạng mức membership.
     */
    private void validateCreditLimit(Customer customer, BigDecimal totalAllocated) {
        BigDecimal masterLimit = resolveMembershipCreditLimit(customer);
        if (totalAllocated.compareTo(masterLimit) > 0) {
            throw new BusinessException("MOD2-UC14-016",
                    "Tổng hạng mức của các phòng cộng lại (" + totalAllocated
                            + ") vượt quá tổng hạng mức của tài khoản tổng (" + masterLimit + ").");
        }
    }

    /**
     * Validate số lượng primary contact của 1 phòng phải đúng bằng 1.
     */
    private void validatePrimaryContactCount(Room room, List<DependentRegistrationDTO> companions,
            boolean isFirstRoom) {
        long primaryCount = isFirstRoom ? 1 : 0;
        for (DependentRegistrationDTO dto : companions) {
            if (!isFirstRoom && Boolean.TRUE.equals(dto.getIsPrimaryContact()))
                primaryCount++;
        }
        if (primaryCount != 1) {
            throw new BusinessException("CHECKIN-006",
                    "Phòng " + room.getRoomNumber() + " phải có đúng 1 người đứng đầu!");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Room Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Lấy Room bằng Pessimistic Lock và validate trạng thái
     * Vacant_Clean/Vacant_Dirty.
     * ADR-UC14-002: Chống overbooking Walk-in.
     */
    private Room findAndValidateRoom(Long roomId) {
        Room room = roomRepository.findByIdWithPessimisticLock(roomId)
                .orElseThrow(() -> new BusinessException("MOD2-UC14-004",
                        "No available rooms found for the requested room ID: " + roomId));

        if (!ROOM_STATUS_VACANT_CLEAN.equalsIgnoreCase(room.getRoomStatus())
                && !ROOM_STATUS_VACANT_DIRTY.equalsIgnoreCase(room.getRoomStatus())) {
            throw new BusinessException("MOD2-UC14-006",
                    "Selected room is not available for check-in. Current status: " + room.getRoomStatus());
        }

        boolean hasPendingMaintenance = maintenanceRequestRepo.existsByRoomIdAndStatusInAndOperationalTypeIn(
                roomId,
                java.util.Arrays.asList("Pending", "InProgress"),
                java.util.Arrays.asList("MAINTENANCE", "DAMAGE_CHECK")
        );
        if (hasPendingMaintenance) {
            throw new BusinessException("MOD2-UC14-017",
                    "Selected room has a pending maintenance/damage check task. Cannot check in.");
        }

        return room;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Guest Classification & Surcharge
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Phân loại số lượng người lớn và trẻ em từ khách chính và danh sách đi kèm.
     * Khách không có dateOfBirth được tính là người lớn (fallback an toàn).
     */
    private GuestCount classifyGuests(LocalDate primaryDob, List<DependentRegistrationDTO> companions,
            List<Integer> childAges) {
        int adults = 0, children = 0;

        if (primaryDob != null) {
            int age = Period.between(primaryDob, LocalDate.now()).getYears();
            if (age >= ADULT_AGE_THRESHOLD)
                adults++;
            else {
                children++;
                childAges.add(age);
            }
        } else {
            adults++; // Fallback: không biết tuổi → tính là người lớn
        }

        for (DependentRegistrationDTO dto : companions) {
            if (dto.getDateOfBirth() != null) {
                int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
                if (age >= ADULT_AGE_THRESHOLD)
                    adults++;
                else {
                    children++;
                    childAges.add(age);
                }
            } else {
                adults++; // Fallback
            }
        }
        return new GuestCount(adults, children);
    }

    /**
     * Validate số khách theo Soft/Hard Limit và tính phụ thu nếu vượt base
     * capacity.
     *
     * @return BigDecimal phụ thu/đêm (ZERO nếu không có)
     */
    private BigDecimal validateAndCalculateSurcharge(GuestCount count, RoomCategory category, List<Integer> childAges) {
        int maxAdults = category.getMaxAdults() != null ? category.getMaxAdults() : category.getCapacity();
        int maxChildren = category.getMaxChildren() != null ? category.getMaxChildren() : 2;
        int baseAdults = category.getBaseAdults() != null ? category.getBaseAdults() : category.getCapacity();
        int baseChildren = category.getBaseChildren() != null ? category.getBaseChildren() : 0;

        // Hard Limit — reject nếu vượt max
        if (count.adults > maxAdults || count.children > maxChildren) {
            throw new BusinessException("MOD2-UC14-009",
                    "Number of guests exceeds maximum room capacity. " +
                            "Max adults: " + maxAdults + ", max children: " + maxChildren);
        }

        // Soft Limit — tính phụ thu người lớn vượt base
        BigDecimal surcharge = BigDecimal.ZERO;
        int extraAdults = Math.max(0, count.adults - baseAdults);
        if (extraAdults > 0 && category.getExtraAdultSurcharge() != null) {
            surcharge = surcharge.add(category.getExtraAdultSurcharge().multiply(BigDecimal.valueOf(extraAdults)));
        }

        // Soft Limit — tính phụ thu trẻ em vượt base (ưu tiên trẻ nhỏ được miễn phí)
        int chargeableChildren = Math.max(0, count.children - baseChildren);
        if (chargeableChildren > 0) {
            Collections.sort(childAges); // nhỏ tuổi nhất được miễn phí trước
            int skipCount = count.children - chargeableChildren;
            for (int i = skipCount; i < childAges.size(); i++) {
                surcharge = surcharge.add(
                        roomSurchargeRepository.findSurchargeForAge(category, childAges.get(i))
                                .map(RoomSurcharge::getPriceModifier)
                                .orElse(BigDecimal.ZERO));
            }
        }
        return surcharge;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Customer & Account Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Find-or-Create Customer. Trả về {@link CustomerLookupResult} để tránh
     * array trick {@code boolean[]} trong lambda closure.
     */
    private CustomerLookupResult findOrCreateCustomerWithFlag(WalkInCheckInRequest req) {
        if (req.getCccd() == null || req.getCccd().isBlank()) {
            return new CustomerLookupResult(createNewCustomer(req, null), true);
        }

        String encryptedCccd = EncryptionUtils.encrypt(req.getCccd());
        return customerRepository.findFirstByCccdPassportEncrypted(encryptedCccd)
                .map(existing -> {
                    boolean updated = false;
                    if (req.getDateOfBirth() != null && !req.getDateOfBirth().equals(existing.getBirthDate())) {
                        existing.setBirthDate(req.getDateOfBirth());
                        updated = true;
                    }
                    if (req.getFullName() != null && !req.getFullName().isBlank()
                            && !req.getFullName().equals(existing.getFullName())) {
                        existing.setFullName(req.getFullName());
                        updated = true;
                    }
                    if (req.getGender() != null && !req.getGender().isBlank()
                            && !req.getGender().equals(existing.getGender())) {
                        existing.setGender(req.getGender());
                        updated = true;
                    }
                    if (req.getPhone() != null && !req.getPhone().isBlank()
                            && !req.getPhone().equals(existing.getPhone())) {
                        existing.setPhone(req.getPhone());
                        updated = true;
                    }
                    if (updated) {
                        customerRepository.save(existing);
                    }
                    return new CustomerLookupResult(existing, false);
                })
                .orElseGet(() -> new CustomerLookupResult(createNewCustomer(req, encryptedCccd), true));
    }

    /**
     * Tạo Customer mới và lưu vào DB.
     * Validate email/phone không trùng trước khi insert.
     */
    private Customer createNewCustomer(WalkInCheckInRequest req, String encryptedCccd) {
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (customerRepository.existsByEmail(req.getEmail())) {
                throw new BusinessException("MOD2-UC14-010",
                        "Email '" + req.getEmail() + "' đã được đăng ký cho một tài khoản khác. "
                                + "Vui lòng sử dụng chức năng tìm kiếm (Check Existing) hoặc dùng Email khác.");
            }
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            if (customerRepository.findFirstByPhone(req.getPhone()).isPresent()) {
                throw new BusinessException("MOD2-UC14-011",
                        "Số điện thoại '" + req.getPhone() + "' đã được đăng ký cho một tài khoản khác. "
                                + "Vui lòng sử dụng chức năng tìm kiếm (Check Existing) hoặc dùng số khác.");
            }
        }

        Customer customer = new Customer();
        customer.setFullName(req.getFullName());
        customer.setPhone(req.getPhone() != null ? req.getPhone() : "");
        customer.setEmail(req.getEmail() != null ? req.getEmail() : "guest_" + UUID.randomUUID() + "@kawai.auto");
        customer.setGender(req.getGender() != null ? req.getGender() : "Unknown");
        customer.setBirthDate(req.getDateOfBirth());
        customer.setCccdPassportEncrypted(encryptedCccd);
        customer.setMembershipTier(membershipTierRepository.findByTierNameIgnoreCase("Regular").orElse(null));
        return customerRepository.save(customer);
    }

    /**
     * Tự động tạo Account cho khách mới (BR-08/09).
     * BR-09: Default password hash không null.
     * BR-10: Account được link vào Customer.
     */
    private Account autoCreateAccount(Customer customer, String email, String rawPassword) {
        Account account = new Account();
        String username = (email != null && !email.isBlank() && email.contains("@"))
                ? email.split("@")[0]
                : "walkin_" + UUID.randomUUID().toString().substring(0, 8);
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setIsActive(true);

        Role role = roleRepository.findByRoleName("CUSTOMER NORMAL")
                .orElseGet(() -> roleRepository.findByRoleName("CUSTOMER").orElse(null));
        if (role != null)
            account.setRole(role);

        Account savedAccount = accountRepository.save(account);
        customer.setAccount(savedAccount);
        return savedAccount;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Entity Build & Persist Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Tạo RoomBooking entity với trạng thái CHECKED_IN và bookingSource=WALK_IN.
     * Booking chuyển sang Pending_Payment nếu thanh toán qua VNPay/Chuyển khoản.
     */
    private RoomBooking buildRoomBooking(WalkInCheckInRequest req, Customer customer) {
        RoomBooking booking = new RoomBooking();
        booking.setCustomer(customer);
        booking.setBookingDate(LocalDate.now());
        booking.setBookingStatus(resolveBookingStatus(req.getPaymentMethod()));
        booking.setBookingSource(BOOKING_SOURCE_WALK_IN);
        booking.setCheckInDate(req.getCheckInDate() != null ? req.getCheckInDate() : LocalDate.now());
        booking.setCheckOutDate(req.getCheckOutDate() != null ? req.getCheckOutDate() : LocalDate.now().plusDays(1));
        booking.setTotalPrice(BigDecimal.ZERO);
        booking.setDepositAmount(BigDecimal.ZERO);
        booking.setCancellationDeadline(LocalDate.now());
        booking.setCreditLimit(resolveMembershipCreditLimit(customer));
        booking.setPersonalPinHash(UUID.randomUUID().toString().substring(0, 8));
        return booking;
    }

    /**
     * Tạo RoomBookingDetail với trạng thái CHECKED_IN và extra surcharge đã tính.
     */
    private RoomBookingDetail buildRoomBookingDetail(WalkInCheckInRequest req, RoomBooking booking,
            Room room, RoomCategory category, GuestCount guestCount, BigDecimal extraSurcharge) {
        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setRoomBooking(booking);
        detail.setRoom(room);
        detail.setCategory(category);
        detail.setDetailStatus(resolveBookingStatus(req.getPaymentMethod()));
        detail.setRoomCharge(category.getBasePrice() != null ? category.getBasePrice() : BigDecimal.ZERO);
        detail.setNumberOfAdults(guestCount.adults);
        detail.setNumberOfChildren(guestCount.children);
        detail.setExtraSurcharge(extraSurcharge.compareTo(BigDecimal.ZERO) > 0 ? extraSurcharge : null);
        return detail;
    }

    /**
     * Lưu RoomGuest cho Master Customer (primary contact của phòng đầu tiên).
     */
    private void buildMasterRoomGuest(RoomBookingDetail detail, Customer customer) {
        RoomGuest masterGuest = new RoomGuest();
        masterGuest.setRoomBookingDetail(detail);
        masterGuest.setCustomer(customer);
        masterGuest.setGuestType(GUEST_TYPE_ADULT);
        masterGuest.setIsPrimaryContact(true);
        roomGuestRepository.saveAndFlush(masterGuest);
    }

    /**
     * Tạo Dependent entity và lưu RoomGuest tương ứng cho khách đi kèm.
     */
    private void buildAndSaveDependentGuest(RoomBookingDetail detail, Customer customer,
            DependentRegistrationDTO dto, boolean isPrimary) {
        Dependent d = new Dependent();
        d.setCustomer(customer);
        d.setDependentName(dto.getFullName() != null && !dto.getFullName().isBlank()
                ? dto.getFullName().trim()
                : DEFAULT_GUEST_NAME);
        d.setGender(dto.getGender() != null ? dto.getGender() : DEFAULT_GENDER);
        d.setBirthDate(dto.getDateOfBirth() != null
                ? dto.getDateOfBirth()
                : LocalDate.now().minusYears(ADULT_AGE_THRESHOLD).withDayOfYear(1));

        if (dto.getCccd() != null && !dto.getCccd().isBlank()) {
            d.setCccdPassportEncrypted(EncryptionUtils.encrypt(dto.getCccd()));
        }
        Dependent saved = dependentRepository.save(d);

        int age = saved.getBirthDate() != null
                ? Period.between(saved.getBirthDate(), LocalDate.now()).getYears()
                : ADULT_AGE_THRESHOLD;

        RoomGuest rg = new RoomGuest();
        rg.setRoomBookingDetail(detail);
        rg.setDependent(saved);
        rg.setIsPrimaryContact(isPrimary);
        rg.setGuestType(age < 12 ? GUEST_TYPE_CHILD : GUEST_TYPE_ADULT);
        roomGuestRepository.saveAndFlush(rg);
    }

    /**
     * Cộng tiền cọc vào Folio nếu là phòng đầu tiên, có tiền cọc, và không phải
     * Chuyển khoản.
     */
    private void applyDepositToFolioIfNeeded(boolean isFirstRoom, BigDecimal totalDeposit,
            WalkInCheckInRequest request, RoomBookingDetail detail) {
        if (!isFirstRoom || totalDeposit.compareTo(BigDecimal.ZERO) <= 0)
            return;

        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : DEFAULT_PAYMENT_METHOD;
        if (!"Chuyển khoản".equalsIgnoreCase(paymentMethod)) {
            folioService.addFolioItem(detail.getId(), "FRONT_DESK", totalDeposit.negate(),
                    "Tiền cọc Walk-in (" + paymentMethod + ")");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Response & Calculation Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Tính tổng phí của 1 RoomBookingDetail theo số đêm. */
    private BigDecimal calculateDetailCharge(RoomBooking booking, RoomBookingDetail detail) {
        long nights = resolveNights(booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal roomCharge = detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO;
        BigDecimal extraSurcharge = detail.getExtraSurcharge() != null ? detail.getExtraSurcharge() : BigDecimal.ZERO;
        return roomCharge.add(extraSurcharge).multiply(BigDecimal.valueOf(nights));
    }

    /** Build WalkInCheckInResponse từ kết quả xử lý. */
    private WalkInCheckInResponse buildWalkInResponse(RoomBooking booking, String firstRoomNumber,
            WalkInCheckInRequest request, Customer customer, boolean isNewCustomer, Account newAccount) {
        WalkInCheckInResponse response = new WalkInCheckInResponse();
        response.setBookingId(booking.getId());

        String displayRoomNumber = request.getRoomSelections().size() > 1
                ? firstRoomNumber + " (+ " + (request.getRoomSelections().size() - 1) + " rooms)"
                : firstRoomNumber;
        response.setRoomNumber(displayRoomNumber);
        response.setBookingStatus(booking.getBookingStatus());
        response.setCustomerId(customer.getId());
        response.setNewCustomer(isNewCustomer);
        response.setAccompaniedGuestCount(
                request.getRoomSelections().stream()
                        .mapToInt(s -> s.getAccompaniedGuests() != null ? s.getAccompaniedGuests().size() : 0)
                        .sum());
        if (newAccount != null)
            response.setNewAccountUsername(newAccount.getUsername());
        return response;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private — Utility / Resolver Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Tổng allocatedCreditLimit từ tất cả roomSelections. */
    private BigDecimal sumAllocatedCreditLimit(WalkInCheckInRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (WalkInRoomSelectionDTO selection : request.getRoomSelections()) {
            if (selection.getAllocatedCreditLimit() != null) {
                total = total.add(selection.getAllocatedCreditLimit());
            }
        }
        return total;
    }

    /**
     * Resolve booking/detail status theo phương thức thanh toán.
     * VNPay/Chuyển khoản → Pending_Payment; các phương thức khác → Checked_In.
     */
    private String resolveBookingStatus(String paymentMethod) {
        if (paymentMethod == null)
            return BOOKING_STATUS_CHECKED_IN;
        String pm = paymentMethod.trim();
        return (pm.equalsIgnoreCase("VNPay") || pm.equalsIgnoreCase("Chuyển khoản"))
                ? BOOKING_STATUS_PENDING_PAYMENT
                : BOOKING_STATUS_CHECKED_IN;
    }

    /**
     * Resolve credit limit từ membership tier, fallback về DEFAULT_CREDIT_LIMIT.
     */
    private BigDecimal resolveMembershipCreditLimit(Customer customer) {
        if (customer.getMembershipTier() != null && customer.getMembershipTier().getCreditLimit() != null) {
            return customer.getMembershipTier().getCreditLimit();
        }
        return DEFAULT_CREDIT_LIMIT;
    }

    /** Resolve deposit amount từ request, fallback về ZERO. */
    private BigDecimal resolveDepositAmount(WalkInCheckInRequest request) {
        return request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO;
    }

    /** Resolve companions list, fallback về emptyList nếu null. */
    private List<DependentRegistrationDTO> resolveCompanions(WalkInRoomSelectionDTO selection) {
        return selection.getAccompaniedGuests() != null ? selection.getAccompaniedGuests() : Collections.emptyList();
    }

    /**
     * Tính số đêm giữa checkIn và checkOut. Tối thiểu 1 đêm.
     */
    private long resolveNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null)
            checkIn = LocalDate.now();
        if (checkOut == null)
            checkOut = LocalDate.now().plusDays(1);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        return nights <= 0 ? 1 : nights;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Private Value Objects
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Kết quả phân loại số lượng khách (người lớn / trẻ em).
     */
    private static class GuestCount {
        final int adults;
        final int children;

        GuestCount(int adults, int children) {
            this.adults = adults;
            this.children = children;
        }
    }

    /**
     * Kết quả tìm kiếm/tạo Customer, bao gồm flag isNew.
     * Thay thế cho array trick {@code boolean[]} trong lambda.
     */
    private static class CustomerLookupResult {
        final Customer customer;
        final boolean isNew;

        CustomerLookupResult(Customer customer, boolean isNew) {
            this.customer = customer;
            this.isNew = isNew;
        }
    }
}
