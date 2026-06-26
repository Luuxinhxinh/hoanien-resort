package com.kawai.services.impl;

import com.kawai.dto.DependentRegistrationDTO;

import com.kawai.dto.walkin.WalkInCheckInRequest;
import com.kawai.dto.walkin.WalkInCheckInResponse;
import com.kawai.dto.walkin.WalkInSurchargeResponse;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.WalkInCheckInService;
import com.kawai.utils.EncryptionUtils;

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
 * Implements toàn bộ 16 bước của luồng Walk-in Check-in theo TDD_UC14_SPEC.md.
 * Tuân thủ ADR-UC14-003 (ACID Transaction) và ADR-UC14-002 (Pessimistic Lock).
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
 * - Người lớn: >= 18 tuổi
 * - Trẻ em : < 18 tuổi
 * - Soft Limit: adults > baseAdults hoặc children > baseChildren → tính extra
 * surcharge
 * - Hard Limit: adults > maxAdults hoặc children > maxChildren → reject
 * (MOD2-UC14-009)
 */
@Service
public class WalkInCheckInServiceImpl implements WalkInCheckInService {

    // ── Dependency Injection ─────────────────────────────────────────────────
    private final RoomRepository roomRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final DependentRepository dependentRepository;
    private final RoomSurchargeRepository roomSurchargeRepository;
    private final com.kawai.repositories.RoomGuestRepository roomGuestRepository;
    private final com.kawai.repositories.RoleRepository roleRepository;
    private final com.kawai.repositories.MembershipTierRepository membershipTierRepository;
    private final com.kawai.services.interfaces.FolioService folioService;
    private final PasswordEncoder passwordEncoder;
    private static final int ADULT_AGE_THRESHOLD = 18;
    private static final String CCCD_PATTERN = "\\d{12}";

    public WalkInCheckInServiceImpl(
            RoomRepository roomRepository,
            RoomBookingRepository roomBookingRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            DependentRepository dependentRepository,
            com.kawai.repositories.RoomSurchargeRepository roomSurchargeRepository,
            com.kawai.repositories.RoomGuestRepository roomGuestRepository,
            com.kawai.repositories.RoleRepository roleRepository,
            com.kawai.repositories.MembershipTierRepository membershipTierRepository,
            com.kawai.services.interfaces.FolioService folioService,
            PasswordEncoder passwordEncoder) {
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
    }

    /**
     * Thực hiện toàn bộ luồng Walk-in Check-in trong 1 ACID Transaction.
     * ADR-UC14-003: Toàn bộ walk-in flow là 1 @Transactional.
     */
    @Override
    @Transactional
    public WalkInCheckInResponse createWalkInBookingAndCheckIn(WalkInCheckInRequest request) {
        try {
            // ── Step 1: Validate thông tin định danh ─────────────────────────
            validateIdentification(request);

            // ── Pre-Validation: Validate phòng và capacity trước khi lưu bất kỳ dữ liệu
            // nào ──
            boolean isFirstForValidation = true;
            BigDecimal totalAllocatedCreditLimit = BigDecimal.ZERO;
            for (com.kawai.dto.walkin.WalkInRoomSelectionDTO selection : request.getRoomSelections()) {
                if (selection.getAllocatedCreditLimit() != null) {
                    totalAllocatedCreditLimit = totalAllocatedCreditLimit.add(selection.getAllocatedCreditLimit());
                }
                Room room = findAndValidateRoom(selection.getRoomId());
                List<DependentRegistrationDTO> companions = selection.getAccompaniedGuests() != null
                        ? selection.getAccompaniedGuests()
                        : Collections.emptyList();
                List<Integer> childAges = new java.util.ArrayList<>();
                LocalDate primaryDob = isFirstForValidation ? request.getDateOfBirth() : null;
                GuestCount guestCount = classifyGuests(primaryDob, companions, childAges);
                validateAndCalculateSurcharge(guestCount, room.getCategory(), childAges);
                isFirstForValidation = false;
            }

            // ── Step 5: Find-or-Create Customer ─────────────────────────────
            boolean[] isNewCustomerHolder = { false };
            Customer customer = findOrCreateCustomer(request, isNewCustomerHolder);

            // ── Step 6: Auto-create Account nếu khách mới (BR-08/09) ────────
            com.kawai.models.Account newAccount = null;
            if (isNewCustomerHolder[0]) {
                newAccount = autoCreateAccount(customer, request.getEmail());
            }

            // Validate Total Allocated Credit Limit
            BigDecimal masterCreditLimit = (customer.getMembershipTier() != null && customer.getMembershipTier().getCreditLimit() != null) 
                    ? customer.getMembershipTier().getCreditLimit() 
                    : new BigDecimal("5000000.00");
            if (totalAllocatedCreditLimit.compareTo(masterCreditLimit) > 0) {
                throw new BusinessException("MOD2-UC14-016",
                        "Tổng hạng mức của các phòng cộng lại (" + totalAllocatedCreditLimit
                                + ") vượt quá tổng hạng mức của tài khoản tổng (" + masterCreditLimit + ").");
            }

            // ── Step 7: Tạo RoomBooking ──────────────────────────────────────
            RoomBooking booking = buildRoomBooking(request, customer);
            booking = roomBookingRepository.save(booking);

            BigDecimal totalDeposit = request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO;
            booking.setDepositAmount(totalDeposit);

            BigDecimal bookingTotalPrice = BigDecimal.ZERO;
            boolean isFirstRoom = true;
            String firstRoomNumber = "";

            for (com.kawai.dto.walkin.WalkInRoomSelectionDTO selection : request.getRoomSelections()) {
                // ── Step 2 & 3: Lock và validate phòng ──────────────────────────
                Room room = findAndValidateRoom(selection.getRoomId());
                if (isFirstRoom) {
                    firstRoomNumber = room.getRoomNumber();
                }
                RoomCategory category = room.getCategory();

                // ── Step 4: Guests Classification & Surcharge Preview ───────
                List<DependentRegistrationDTO> companions = selection.getAccompaniedGuests() != null
                        ? selection.getAccompaniedGuests()
                        : Collections.emptyList();

                List<Integer> childAges = new java.util.ArrayList<>();
                LocalDate primaryDob = isFirstRoom ? request.getDateOfBirth() : null;

                GuestCount guestCount = classifyGuests(primaryDob, companions, childAges);
                BigDecimal extraSurcharge = validateAndCalculateSurcharge(guestCount, category, childAges);

                // ── Step 8: Tạo RoomBookingDetail với extra surcharge ─────────────
                RoomBookingDetail detail = buildRoomBookingDetail(request, booking, room, category,
                        guestCount, extraSurcharge);
                detail.setSubCreditLimit(
                        selection.getAllocatedCreditLimit() != null ? selection.getAllocatedCreditLimit()
                                : BigDecimal.ZERO);
                roomBookingDetailRepository.save(detail);

                // Cập nhật giá booking master
                long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(),
                        booking.getCheckOutDate());
                if (nights <= 0)
                    nights = 1;

                BigDecimal detailRoomCharge = detail.getRoomCharge() != null ? detail.getRoomCharge() : BigDecimal.ZERO;
                BigDecimal detailSurcharge = detail.getExtraSurcharge() != null ? detail.getExtraSurcharge()
                        : BigDecimal.ZERO;

                BigDecimal totalDetailCharge = detailRoomCharge.add(detailSurcharge)
                        .multiply(BigDecimal.valueOf(nights));
                bookingTotalPrice = bookingTotalPrice.add(totalDetailCharge);

                room.setRoomStatus("Occupied");
                roomRepository.save(room);

                // ── Step 10: Residence Reporting (Master Customer & Dependents) ───────
                if (isFirstRoom) {
                    // Cố định Master Customer LUÔN LÀ người đứng đầu phòng đầu tiên
                    RoomGuest masterGuest = new RoomGuest();
                    masterGuest.setRoomBookingDetail(detail);
                    masterGuest.setCustomer(customer);
                    masterGuest.setGuestType("ADULT");
                    masterGuest.setIsPrimaryContact(true);
                    roomGuestRepository.saveAndFlush(masterGuest);
                }

                for (DependentRegistrationDTO dto : companions) {
                    Dependent d = new Dependent();
                    d.setCustomer(customer);
                    d.setDependentName(dto.getFullName());
                    d.setBirthDate(dto.getDateOfBirth());
                    d.setGender(dto.getGender() != null ? dto.getGender() : "Khác");
                    if (dto.getCccd() != null && !dto.getCccd().isBlank()) {
                        d.setCccdPassportEncrypted(EncryptionUtils.encrypt(dto.getCccd()));
                    }
                    Dependent savedDep = dependentRepository.save(d);

                    // Tạo liên kết RoomGuest cho người đi kèm
                    RoomGuest rg = new RoomGuest();
                    rg.setRoomBookingDetail(detail);
                    rg.setDependent(savedDep);
                    // Nếu là phòng 1, cưỡng chế không cho khách đi kèm làm Đứng đầu (vì Master
                    // Guest đã gánh).
                    rg.setIsPrimaryContact(isFirstRoom ? false
                            : (dto.getIsPrimaryContact() != null ? dto.getIsPrimaryContact() : false));

                    int age = 18;
                    if (savedDep.getBirthDate() != null) {
                        age = Period.between(savedDep.getBirthDate(), LocalDate.now()).getYears();
                    }
                    rg.setGuestType(age < 12 ? "CHILD" : "ADULT");
                    roomGuestRepository.saveAndFlush(rg);
                }

                // Backend Validation: Kiểm tra số lượng primary contact của phòng này phải đúng
                // bằng 1
                long primaryCount = isFirstRoom ? 1 : 0;
                for (DependentRegistrationDTO dto : companions) {
                    if (!isFirstRoom && Boolean.TRUE.equals(dto.getIsPrimaryContact())) {
                        primaryCount++;
                    }
                }
                if (primaryCount != 1) {
                    throw new BusinessException("CHECKIN-006",
                            "Phòng " + room.getRoomNumber() + " phải có đúng 1 người đứng đầu!");
                }

                // ── Step 11: Payment & Folio Initialization ──────────────────────
                if (isFirstRoom && totalDeposit.compareTo(BigDecimal.ZERO) > 0) {
                    String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "Tiền mặt";
                    if (!"Chuyển khoản".equalsIgnoreCase(paymentMethod)) {
                        folioService.addFolioItem(detail.getId(), "FRONT_DESK", totalDeposit.negate(),
                                "Tiền cọc Walk-in (" + paymentMethod + ")");
                    }
                }

                isFirstRoom = false;
            }

            booking.setTotalPrice(bookingTotalPrice);
            roomBookingRepository.save(booking);

            // ── Build & Return Response ──────────────────────────────────────
            WalkInCheckInResponse response = new WalkInCheckInResponse();
            response.setBookingId(booking.getId());
            response.setRoomNumber(request.getRoomSelections().size() > 1
                    ? firstRoomNumber + " (+ " + (request.getRoomSelections().size() - 1) + " rooms)"
                    : firstRoomNumber);
            response.setBookingStatus(booking.getBookingStatus());
            response.setCustomerId(customer.getId());
            response.setNewCustomer(isNewCustomerHolder[0]);

            int totalCompanions = request.getRoomSelections().stream()
                    .mapToInt(s -> s.getAccompaniedGuests() != null ? s.getAccompaniedGuests().size() : 0)
                    .sum();
            response.setAccompaniedGuestCount(totalCompanions);
            if (newAccount != null) {
                response.setNewAccountUsername(newAccount.getUsername());
                response.setNewAccountPassword("123456"); // Show plain-text default password
            }

            return response;

        } catch (BusinessException ex) {
            // Re-throw BusinessException trực tiếp (đã có errorCode + message)
            throw ex;
        } catch (Exception ex) {
            // Bất kỳ lỗi không mong muốn nào → MOD2-UC14-005, rollback toàn bộ
            throw new BusinessException("MOD2-UC14-005",
                    "Walk-in check-in failed. Transaction rolled back: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void cancelPendingWalkIn(Long bookingId) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("MOD2-UC14-012", "Booking not found"));

        if (!"WALK_IN".equals(booking.getBookingSource()) || !"Pending_Payment".equals(booking.getBookingStatus())) {
            throw new BusinessException("MOD2-UC14-013",
                    "Only Walk-in bookings with Pending_Payment can be cancelled via this API");
        }

        booking.setBookingStatus("Cancelled");
        List<com.kawai.models.RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
        for (com.kawai.models.RoomBookingDetail detail : details) {
            detail.setDetailStatus("Cancelled");
            if (detail.getRoom() != null) {
                com.kawai.models.Room room = detail.getRoom();
                room.setRoomStatus("Vacant_Clean");
                roomRepository.save(room);
            }
        }
        roomBookingDetailRepository.saveAll(details);
        roomBookingRepository.save(booking);
    }

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

        for (com.kawai.dto.walkin.WalkInRoomSelectionDTO selection : request.getRoomSelections()) {
            Room room = roomRepository.findById(selection.getRoomId())
                    .orElseThrow(() -> new BusinessException("MOD2-UC14-004",
                            "Room not found for ID: " + selection.getRoomId()));
            RoomCategory category = room.getCategory();
            totalBasePricePerNight = totalBasePricePerNight
                    .add(category.getBasePrice() != null ? category.getBasePrice() : BigDecimal.ZERO);

            List<DependentRegistrationDTO> companions = selection.getAccompaniedGuests() != null
                    ? selection.getAccompaniedGuests()
                    : Collections.emptyList();

            List<Integer> childAges = new java.util.ArrayList<>();

            LocalDate primaryDob = isFirstRoom ? request.getDateOfBirth() : null;
            isFirstRoom = false;

            GuestCount guestCount = classifyGuests(primaryDob, companions, childAges);
            totalAdults += guestCount.adults;
            totalChildren += guestCount.children;

            try {
                BigDecimal roomSurcharge = validateAndCalculateSurcharge(guestCount, category, childAges);
                totalExtraSurcharge = totalExtraSurcharge.add(roomSurcharge);
            } catch (BusinessException ex) {
                throw new BusinessException(ex.getErrorCode(),
                        "Phòng " + room.getRoomNumber() + ": " + ex.getMessage());
            }
        }

        java.time.LocalDate checkIn = request.getCheckInDate() != null ? request.getCheckInDate()
                : java.time.LocalDate.now();
        java.time.LocalDate checkOut = request.getCheckOutDate() != null ? request.getCheckOutDate()
                : java.time.LocalDate.now().plusDays(1);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0)
            nights = 1;

        BigDecimal totalBaseRoomPrice = totalBasePricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal totalSurchargeAllNights = totalExtraSurcharge.multiply(BigDecimal.valueOf(nights));
        BigDecimal totalCharge = totalBaseRoomPrice.add(totalSurchargeAllNights);

        String msg = "Phụ thu dự tính: " + totalExtraSurcharge + "/đêm";
        if (totalExtraSurcharge.compareTo(BigDecimal.ZERO) == 0) {
            msg = "Miễn phí phụ thu (số khách nằm trong sức chứa tiêu chuẩn)";
        }

        BigDecimal suggestedDeposit = totalCharge.multiply(new BigDecimal("0.3")).setScale(0,
                java.math.RoundingMode.HALF_UP);

        return new WalkInSurchargeResponse(totalSurchargeAllNights, totalAdults, totalChildren, msg,
                totalBaseRoomPrice, totalCharge, suggestedDeposit);
    }

    private void validateIdentification(WalkInCheckInRequest req) {
        // Validate dateOfBirth
        if (req.getDateOfBirth() == null) {
            throw new BusinessException("MOD2-UC14-001", "Date of birth is required");
        }

        // Validate CCCD format (nếu có) — 12 chữ số
        String cccd = req.getCccd();
        if (cccd != null && !cccd.isBlank() && !cccd.matches(CCCD_PATTERN)) {
            throw new BusinessException("MOD2-UC14-003",
                    "Invalid identification document: CCCD must be 12 digits");
        }
    }

    /**
     * Lock phòng bằng Pessimistic Lock và validate trạng thái (Step 2 & 3).
     */
    private Room findAndValidateRoom(Long roomId) {
        Room room = roomRepository.findByIdWithPessimisticLock(roomId)
                .orElseThrow(() -> new BusinessException("MOD2-UC14-004",
                        "No available rooms found for the requested room ID: " + roomId));

        if (!"Vacant_Clean".equalsIgnoreCase(room.getRoomStatus()) && !"Vacant_Dirty".equalsIgnoreCase(room.getRoomStatus())) {
            throw new BusinessException("MOD2-UC14-006",
                    "Selected room is not available for check-in. Current status: " + room.getRoomStatus());
        }
        return room;
    }

    /**
     * Phân loại số lượng người lớn và trẻ em từ khách chính và danh sách đi kèm.
     */
    private GuestCount classifyGuests(LocalDate primaryDob, List<DependentRegistrationDTO> companions,
            List<Integer> childAges) {
        int adults = 0;
        int children = 0;

        // Phân loại khách chính
        if (primaryDob != null) {
            int age = Period.between(primaryDob, LocalDate.now()).getYears();
            if (age >= ADULT_AGE_THRESHOLD)
                adults++;
            else {
                children++;
                childAges.add(age);
            }
        } else {
            adults++;
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
     */
    private BigDecimal validateAndCalculateSurcharge(GuestCount count, RoomCategory category, List<Integer> childAges) {
        int maxAdults = category.getMaxAdults() != null ? category.getMaxAdults() : category.getCapacity();
        int maxChildren = category.getMaxChildren() != null ? category.getMaxChildren() : 2;
        int baseAdults = category.getBaseAdults() != null ? category.getBaseAdults() : category.getCapacity();
        int baseChildren = category.getBaseChildren() != null ? category.getBaseChildren() : 0;

        // Hard Limit check
        if (count.adults > maxAdults || count.children > maxChildren) {
            throw new BusinessException("MOD2-UC14-009",
                    "Number of guests exceeds maximum room capacity. " +
                            "Max adults: " + maxAdults + ", max children: " + maxChildren);
        }

        // Tính extra surcharge cho người lớn và trẻ em vượt base capacity
        BigDecimal surcharge = BigDecimal.ZERO;

        int extraAdults = Math.max(0, count.adults - baseAdults);

        if (extraAdults > 0 && category.getExtraAdultSurcharge() != null) {
            surcharge = surcharge.add(
                    category.getExtraAdultSurcharge().multiply(BigDecimal.valueOf(extraAdults)));
        }

        int chargeableChildrenCount = Math.max(0, count.children - baseChildren);
        if (chargeableChildrenCount > 0) {
            Collections.sort(childAges); // Ưu tiên trẻ em nhỏ tuổi được miễn phí (baseChildren)
            int skipCount = Math.max(0, count.children - chargeableChildrenCount);

            for (int i = skipCount; i < childAges.size(); i++) {
                int age = childAges.get(i);
                BigDecimal childSurcharge = roomSurchargeRepository.findSurchargeForAge(category, age)
                        .map(RoomSurcharge::getPriceModifier)
                        .orElse(BigDecimal.ZERO);
                surcharge = surcharge.add(childSurcharge);
            }
        }

        return surcharge;
    }

    /**
     * Find-or-Create Customer profile.
     */
    private Customer findOrCreateCustomer(WalkInCheckInRequest req, boolean[] isNewCustomerHolder) {
        if (req.getCccd() == null || req.getCccd().isBlank()) {
            // Không có CCCD → tạo khách mới ngay
            isNewCustomerHolder[0] = true;
            return createNewCustomer(req, null);
        }

        // Encrypt CCCD để tìm kiếm
        final String encryptedCccd = EncryptionUtils.encrypt(req.getCccd());

        // Tìm khách cũ qua CCCD encrypted — effectively final trong lambda
        return customerRepository.findByCccdPassportEncrypted(encryptedCccd)
                .orElseGet(() -> {
                    isNewCustomerHolder[0] = true;
                    return createNewCustomer(req, encryptedCccd);
                });
    }

    /**
     * Tạo Customer mới và lưu vào DB.
     */
    private Customer createNewCustomer(WalkInCheckInRequest req, String encryptedCccd) {
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (customerRepository.existsByEmail(req.getEmail())) {
                throw new BusinessException("MOD2-UC14-010", "Email '" + req.getEmail()
                        + "' đã được đăng ký cho một tài khoản khác. Vui lòng sử dụng chức năng tìm kiếm (Check Existing) hoặc dùng Email khác.");
            }
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            if (customerRepository.findByPhone(req.getPhone()).isPresent()) {
                throw new BusinessException("MOD2-UC14-011", "Số điện thoại '" + req.getPhone()
                        + "' đã được đăng ký cho một tài khoản khác. Vui lòng sử dụng chức năng tìm kiếm (Check Existing) hoặc dùng số khác.");
            }
        }

        Customer customer = new Customer();
        customer.setFullName(req.getFullName());
        customer.setPhone(req.getPhone() != null ? req.getPhone() : "");
        customer.setEmail(req.getEmail() != null ? req.getEmail() : "guest_" + UUID.randomUUID() + "@kawai.auto");
        customer.setGender(req.getGender() != null ? req.getGender() : "Unknown");
        customer.setCccdPassportEncrypted(encryptedCccd);
        customer.setMembershipTier(membershipTierRepository.findByTierNameIgnoreCase("Regular").orElse(null));
        return customerRepository.save(customer);
    }

    /**
     * Tự động tạo Account cho khách mới (BR-08/09).
     * BR-09: Default password hash không null.
     * BR-10: Account được link vào Customer.
     */
    private com.kawai.models.Account autoCreateAccount(Customer customer, String email) {
        com.kawai.models.Account account = new com.kawai.models.Account();
        // Username = prefix của email hoặc random nếu không có email
        String username = (email != null && !email.isBlank() && email.contains("@"))
                ? email.split("@")[0]
                : "walkin_" + UUID.randomUUID().toString().substring(0, 8);
        account.setUsername(username);

        // Mật khẩu mặc định là 123456 cho khách Walk-in chưa có tài khoản
        String defaultPassword = "123456";
        account.setPasswordHash(passwordEncoder.encode(defaultPassword));
        account.setIsActive(true);

        com.kawai.models.Role role = roleRepository.findByRoleName("CUSTOMER NORMAL")
                .orElseGet(() -> roleRepository.findByRoleName("CUSTOMER").orElse(null));
        if (role != null) {
            account.setRole(role);
        }

        com.kawai.models.Account savedAccount = accountRepository.save(account);
        customer.setAccount(savedAccount);
        return savedAccount;
    }

    /**
     * Tạo RoomBooking entity với trạng thái CHECKED_IN và bookingSource=WALK_IN.
     */
    private RoomBooking buildRoomBooking(WalkInCheckInRequest req, Customer customer) {
        RoomBooking booking = new RoomBooking();
        booking.setCustomer(customer);
        booking.setBookingDate(LocalDate.now());
        String paymentMethod = req.getPaymentMethod() != null ? req.getPaymentMethod() : "";
        if (paymentMethod.equalsIgnoreCase("VNPay") || paymentMethod.equalsIgnoreCase("Chuyển khoản")) {
            booking.setBookingStatus("Pending_Payment");
        } else {
            booking.setBookingStatus("Checked_In");
        }
        booking.setBookingSource("WALK_IN");
        booking.setCheckInDate(req.getCheckInDate() != null ? req.getCheckInDate() : LocalDate.now());
        booking.setCheckOutDate(req.getCheckOutDate() != null ? req.getCheckOutDate() : LocalDate.now().plusDays(1));
        booking.setTotalPrice(BigDecimal.ZERO);
        booking.setDepositAmount(BigDecimal.ZERO);
        booking.setCancellationDeadline(LocalDate.now());
        booking.setCreditLimit(
                (customer.getMembershipTier() != null && customer.getMembershipTier().getCreditLimit() != null)
                        ? customer.getMembershipTier().getCreditLimit()
                        : new BigDecimal("5000000.00"));
        booking.setPersonalPinHash(UUID.randomUUID().toString().substring(0, 8));
        return booking;
    }

    /**
     * Tạo RoomBookingDetail với trạng thái CHECKED_IN và extra surcharge đã tính.
     */
    private RoomBookingDetail buildRoomBookingDetail(WalkInCheckInRequest req, RoomBooking booking, Room room,
            RoomCategory category, GuestCount guestCount, BigDecimal extraSurcharge) {
        RoomBookingDetail detail = new RoomBookingDetail();
        detail.setRoomBooking(booking);
        detail.setRoom(room);
        detail.setCategory(category);
        String paymentMethod = req.getPaymentMethod() != null ? req.getPaymentMethod() : "";
        if (paymentMethod.equalsIgnoreCase("VNPay") || paymentMethod.equalsIgnoreCase("Chuyển khoản")) {
            detail.setDetailStatus("Pending_Payment");
        } else {
            detail.setDetailStatus("Checked_In");
        }
        detail.setRoomCharge(category.getBasePrice() != null ? category.getBasePrice() : BigDecimal.ZERO);
        detail.setNumberOfAdults(guestCount.adults);
        detail.setNumberOfChildren(guestCount.children);
        detail.setExtraSurcharge(extraSurcharge.compareTo(BigDecimal.ZERO) > 0 ? extraSurcharge : null);
        return detail;
    }

    @Override
    public java.util.Optional<com.kawai.models.Customer> searchCustomer(String keyword) {
        if (keyword == null || keyword.isBlank())
            return java.util.Optional.empty();

        java.util.Optional<com.kawai.models.Customer> byPhone = customerRepository.findByPhone(keyword);
        if (byPhone.isPresent()) {
            return byPhone;
        }
        try {
            String encryptedCccd = com.kawai.utils.EncryptionUtils.encrypt(keyword);
            return customerRepository.findByCccdPassportEncrypted(encryptedCccd);
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    private static class GuestCount {
        final int adults;
        final int children;

        GuestCount(int adults, int children) {
            this.adults = adults;
            this.children = children;
        }
    }

}
