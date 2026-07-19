package com.kawai.services.impl;

import com.kawai.dto.DependentRegistrationDTO;
import com.kawai.dto.DependentResponseDTO;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.Dependent;
import com.kawai.models.RoomBooking;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.RoomGuest;
import com.kawai.models.RoomSurcharge;
import com.kawai.repositories.DependentRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomGuestRepository;
import com.kawai.repositories.RoomSurchargeRepository;
import com.kawai.services.interfaces.DependentService;
import com.kawai.services.interfaces.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DependentServiceImpl — UC16: Register Accompanying Guests
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CHANGELOG:
 * 2026-06-30 | Antigravity AI | REFACTOR (Clean Code): tách private methods,
 * | | loại bỏ duplicate null-check, đặt tên constants,
 * | | fix thiếu status="REGISTERED" trong response DTO.
 * 2026-06-20 | Antigravity AI | GREEN: implement DependentServiceImpl, pass 9
 * TC.
 *
 * Business Rules:
 * BR-SYS-01 : CCCD/Hộ chiếu phải mã hoá AES-256 trước khi lưu DB
 * ADR-002 : Kiểm tra trùng lặp theo booking_id + cccd_encrypted
 *
 * Error Codes:
 * MOD2-003 : Booking not found (404)
 * MOD2-015 : Reservation is not active (400)
 * MOD2-016 : Duplicate guest registration (409)
 * MOD2-017 : Invalid identification document (422)
 */
@Service
public class DependentServiceImpl implements DependentService {

    private static final Logger log = LoggerFactory.getLogger(DependentServiceImpl.class);

    // ── Business Constants ──────────────────────────────────────────────────
    /** Trạng thái booking được phép thêm dependent (Invariant §6.5 EDS). */
    private static final Set<String> ACTIVE_BOOKING_STATUSES = Set.of("Confirmed", "Checked_In");
    private static final int ADULT_AGE_THRESHOLD = 18;
    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String DEFAULT_DEPENDENT_NAME = "Khách đi kèm";
    private static final String DEFAULT_GENDER = "Khác";
    private static final String GUEST_TYPE_CHILD = "CHILD";
    private static final String GUEST_TYPE_ADULT = "ADULT";
    private static final String FACE_UPLOAD_DIR = com.kawai.utils.UploadPathResolver
            .resolvePath("src/main/resources/static/uploads/faces");
    private final DependentRepository dependentRepository;
    private final RoomBookingRepository bookingRepository;
    private final EncryptionService encryptionService;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomGuestRepository roomGuestRepository;
    private final RoomSurchargeRepository roomSurchargeRepository;
    private final com.kawai.services.interfaces.CheckinService checkinService;
    private final com.cloudinary.Cloudinary cloudinary;

    public DependentServiceImpl(
            DependentRepository dependentRepository,
            RoomBookingRepository bookingRepository,
            EncryptionService encryptionService,
            RoomBookingDetailRepository roomBookingDetailRepository,
            RoomGuestRepository roomGuestRepository,
            RoomSurchargeRepository roomSurchargeRepository,
            @Lazy com.kawai.services.interfaces.CheckinService checkinService,
            com.cloudinary.Cloudinary cloudinary) {
        this.dependentRepository = dependentRepository;
        this.bookingRepository = bookingRepository;
        this.encryptionService = encryptionService;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomGuestRepository = roomGuestRepository;
        this.roomSurchargeRepository = roomSurchargeRepository;
        this.checkinService = checkinService;
        this.cloudinary = cloudinary;
    }

    /**
     * Đăng ký khách đi kèm (Dependent) cho một booking.
     *
     * Flow:
     * 1. Validate CCCD format (MOD2-017)
     * 2. Validate Date of Birth
     * 3. Tìm Booking (MOD2-003)
     * 4. Kiểm tra Booking status (MOD2-015)
     * 5. Mã hoá CCCD AES-256 (BR-SYS-01)
     * 6. Kiểm tra trùng lặp CCCD trong booking (MOD2-016)
     * 7. Lưu Dependent entity
     * 8. Lưu ảnh FaceID nếu có
     * 9. Xử lý RoomBookingDetail + phụ thu nếu có
     * 10. Build và trả về DependentResponseDTO
     */
    @Override
    @Transactional
    public DependentResponseDTO registerDependent(Long bookingId, DependentRegistrationDTO dto) {
        // Steps 1–2: Input validation (fail-fast trước khi truy cập DB)
        validateCccdIfPresent(dto.getCccd());
        validateDateOfBirthNotFuture(dto.getDateOfBirth());

        // Steps 3–4: Business-state validation
        RoomBooking booking = findBookingOrThrow(bookingId);
        assertBookingIsActive(booking);

        // Step 5: Encrypt PII (BR-SYS-01 — Nghị định 13/2023)
        String cccdEncrypted = encryptCccd(dto.getCccd());

        // Step 6: Duplicate check (ADR-002)
        if (isNewRegistration(dto)) {
            assertNoDuplicateCccd(bookingId, booking, cccdEncrypted);
        }

        // Lưu lại ngày sinh CŨ TRƯỚC KHI ghi đè — dùng để hoàn phụ thu trẻ em nếu bị
        // nâng cấp lên ADULT
        LocalDate oldBirthDate = null;
        if (!isNewRegistration(dto)) {
            oldBirthDate = dependentRepository.findById(dto.getDependentId())
                    .map(Dependent::getBirthDate).orElse(null);
        }

        // Steps 7–8: Persist Dependent (ghi đè ngày sinh mới vào DB)
        Dependent saved = buildAndSaveDependent(dto, booking, cccdEncrypted);
        saveFaceImageIfPresent(saved, dto.getFaceImageBase64());

        // Step 9: Handle room-level guest linkage and surcharges
        if (dto.getRoomBookingDetailId() != null) {
            linkGuestToRoomDetail(saved, dto, booking, oldBirthDate);
        }

        log.info("[UC16] Dependent registered: bookingId={}, dependentId={}", bookingId, saved.getId());

        // Step 10: Build response
        return buildResponseDTO(saved, dto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DependentResponseDTO> getGuestListByBooking(Long bookingId) {
        RoomBooking booking = findBookingOrThrow(bookingId); // guard: ném MOD2-003 nếu không tồn tại

        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
        List<DependentResponseDTO> result = new ArrayList<>();

        for (RoomBookingDetail detail : details) {
            List<RoomGuest> guests = roomGuestRepository.findByRoomBookingDetailId(detail.getId());
            for (RoomGuest guest : guests) {
                // Bỏ qua Chủ đơn (Customer) vì đã hiển thị ở phần thông tin chung.
                // Các Customer khác (ví dụ: người đi cùng đã được nâng cấp lên Customer) vẫn
                // được lấy để hiển thị.
                if (guest.getCustomer() != null && guest.getCustomer().getId().equals(booking.getCustomer().getId())) {
                    continue;
                }
                result.add(mapGuestToResponseDTO(guest, detail));
            }
        }
        return result;
    }

    /**
     * Validate CCCD/Hộ chiếu nếu giá trị không rỗng. (BR-SYS-01, MOD2-017)
     * Validate được thực hiện TRƯỚC khi truy cập bất kỳ repository nào (fail-fast).
     */
    private void validateCccdIfPresent(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            return; // CCCD is optional for dependents (e.g. children)
        }
        if (!com.kawai.utils.ValidationUtils.isValidDocument(cccd)) {
            throw new BusinessException("MOD2-017",
                    "Invalid identification document: Must be a 12-digit CCCD or a valid Passport [MOD2-017]");
        }
    }

    /** Validate ngày sinh không được ở tương lai. */
    private void validateDateOfBirthNotFuture(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new BusinessException("MOD2-001",
                    "Invalid Date of Birth: Cannot be in the future [MOD2-001]");
        }
    }

    /** Lấy RoomBooking theo ID hoặc ném MOD2-003. */
    private RoomBooking findBookingOrThrow(Long bookingId) {
        com.kawai.models.Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("MOD2-003",
                        "Booking not found with ID: " + bookingId + " [MOD2-003]"));
        if (!(booking instanceof RoomBooking)) {
            throw new BusinessException("MOD2-003",
                    "Booking ID " + bookingId + " is not a Room Booking [MOD2-003]");
        }
        return (RoomBooking) booking;
    }

    /**
     * Kiểm tra booking phải ở trạng thái ACTIVE (Confirmed / Checked_In).
     * (MOD2-015)
     */
    private void assertBookingIsActive(RoomBooking booking) {
        if (!ACTIVE_BOOKING_STATUSES.contains(booking.getBookingStatus())) {
            throw new BusinessException("MOD2-015",
                    "Reservation is not active (must be Confirmed or Checked_In). " +
                            "Current status: " + booking.getBookingStatus() + " [MOD2-015]");
        }
    }

    /**
     * Mã hoá CCCD bằng AES-256. (BR-SYS-01 — Nghị định 13/2023/NĐ-CP)
     * Được gọi sau khi validate format — đảm bảo cccd không bao giờ null/blank ở
     * đây.
     */
    private String encryptCccd(String cccd) {
        if (cccd == null || cccd.isBlank())
            return null;
        try {
            return com.kawai.utils.EncryptionUtils.encrypt(cccd);
        } catch (Exception e) {
            log.error("[UC16] AES-256 encryption failed for dependent registration: {}", e.getMessage());
            throw new BusinessException("MOD2-005", "Internal error during CCCD encryption [MOD2-005]");
        }
    }

    /** @return true khi dto đang tạo mới (không phải cập nhật). */
    private boolean isNewRegistration(DependentRegistrationDTO dto) {
        return dto.getDependentId() == null;
    }

    /**
     * Kiểm tra không có CCCD trùng trong cùng booking. (ADR-002, MOD2-016)
     *
     * Phạm vi check (trong 1 đơn booking):
     * 1. Dependent khác đã được link vào booking này (qua RoomGuest)
     * 2. Customer đứng đầu booking
     */
    private void assertNoDuplicateCccd(Long bookingId, RoomBooking booking, String cccdEncrypted) {
        if (cccdEncrypted == null)
            return;

        // Check 1: trùng với dependent khác trong cùng booking
        if (dependentRepository.countDuplicateInBooking(bookingId, cccdEncrypted) > 0) {
            throw new BusinessException("MOD2-016",
                    "Căn cước bị trùng với người khác trong cùng một đơn đặt phòng [MOD2-016]");
        }

        // Check 2: trùng với CCCD của customer đứng đầu booking
        String customerCccd = booking.getCustomer() != null
                ? booking.getCustomer().getCccdPassportEncrypted()
                : null;
        if (customerCccd != null && customerCccd.equals(cccdEncrypted)) {
            throw new BusinessException("MOD2-016",
                    "Căn cước bị trùng với người đặt phòng chính trong cùng một đơn đặt phòng [MOD2-016]");
        }
    }

    /**
     * Tạo hoặc cập nhật Dependent entity và lưu vào DB.
     * Trả về entity đã được persist (có ID).
     */
    private Dependent buildAndSaveDependent(DependentRegistrationDTO dto, RoomBooking booking, String cccdEncrypted) {
        Dependent dependent = isNewRegistration(dto)
                ? createNewDependent(booking)
                : loadExistingDependent(dto.getDependentId());

        populateDependentFields(dependent, dto, cccdEncrypted);
        return dependentRepository.save(dependent);
    }

    /** Tạo mới Dependent entity và gán Customer từ booking. */
    private Dependent createNewDependent(RoomBooking booking) {
        Dependent dependent = new Dependent();
        dependent.setCustomer(booking.getCustomer());
        return dependent;
    }

    /** Load Dependent đã tồn tại để cập nhật. */
    private Dependent loadExistingDependent(Long dependentId) {
        return dependentRepository.findById(dependentId)
                .orElseThrow(() -> new BusinessException("MOD2-018",
                        "Dependent not found with ID: " + dependentId));
    }

    private void populateDependentFields(Dependent dependent, DependentRegistrationDTO dto, String cccdEncrypted) {
        dependent.setDependentName(resolveFullName(dto.getFullName()));
        dependent.setBirthDate(resolveBirthDate(dto.getDateOfBirth()));
        dependent.setGender(dto.getGender() != null ? dto.getGender() : DEFAULT_GENDER);
        dependent.setCccdPassportEncrypted(cccdEncrypted); // BR-SYS-01: lưu đã mã hoá

        if (dto.getFaceVectorData() != null && !dto.getFaceVectorData().isEmpty()) {
            dependent.setFaceVectorData(dto.getFaceVectorData());
        }
    }

    private String resolveFullName(String fullName) {
        return (fullName != null && !fullName.isBlank()) ? fullName.trim() : DEFAULT_DEPENDENT_NAME;
    }

    private LocalDate resolveBirthDate(LocalDate dateOfBirth) {
        return dateOfBirth != null ? dateOfBirth : LocalDate.now().minusYears(ADULT_AGE_THRESHOLD).withDayOfYear(1);
    }

    private void saveFaceImageIfPresent(Dependent saved, String faceImageBase64) {
        if (faceImageBase64 == null || faceImageBase64.isEmpty())
            return;
        try {
            // Upload trực tiếp chuỗi Data URI (Base64) lên Cloudinary
            java.util.Map<String, Object> uploadResult = cloudinary.uploader().upload(faceImageBase64,
                    com.cloudinary.utils.ObjectUtils.asMap(
                            "folder", "kawai_faces",
                            "public_id", "dep_" + saved.getId() + "_" + System.currentTimeMillis()));
            String publicUrl = uploadResult.get("secure_url").toString();
            saved.setFaceImgUrl(publicUrl);
            dependentRepository.save(saved);
        } catch (Exception e) {
            log.error("Failed to save FaceID image to Cloudinary for dependent {}", saved.getId(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private — Room Linkage & Surcharge Helpers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Liên kết Dependent vào RoomBookingDetail và tính phụ thu theo độ tuổi nếu
     * cần.
     *
     * @param oldBirthDate Ngày sinh CŨ của Dependent (trước khi bị ghi đè), dùng để
     *                     hoàn phụ thu
     *                     trẻ em khi nâng cấp CHILD → ADULT. Null nếu đây là đăng
     *                     ký mới.
     */
    private void linkGuestToRoomDetail(Dependent saved, DependentRegistrationDTO dto,
            RoomBooking booking, LocalDate oldBirthDate) {
        RoomBookingDetail detail = roomBookingDetailRepository.findById(dto.getRoomBookingDetailId())
                .orElseThrow(() -> new BusinessException("MOD2-019", "RoomBookingDetail not found"));

        int age = calculateAge(saved.getBirthDate());
        boolean isAdultNow = age >= ADULT_AGE_THRESHOLD;

        if (isNewRegistration(dto)) {
            // Khách đi kèm mới hoàn toàn: tính phụ thu bình thường theo độ tuổi thực
            BigDecimal extraFee = applyGuestCountAndCalculateSurcharge(detail, booking, age, detail.getCategory());
            persistDetailAndBookingIfSurcharge(detail, booking, extraFee, saved.getId());
        } else {
            // Cập nhật thông tin khách đã có: kiểm tra có nâng cấp CHILD → ADULT không
            Optional<RoomGuest> existingGuestOpt = roomGuestRepository.findFirstByDependentId(saved.getId());
            if (existingGuestOpt.isPresent()) {
                RoomGuest existingGuest = existingGuestOpt.get();
                if (GUEST_TYPE_CHILD.equals(existingGuest.getGuestType()) && isAdultNow) {
                    // Bước 1: Hoàn lại phụ thu trẻ em đã thu — dùng ngày sinh CŨ (trước khi ghi đè)
                    int oldAge = calculateAge(oldBirthDate); // dùng oldBirthDate truyền vào, không phải saved!
                    Optional<RoomSurcharge> oldChildSurchargeOpt = roomSurchargeRepository
                            .findSurchargeForAge(detail.getCategory(), oldAge);
                    BigDecimal oldChildFeePerNight = oldChildSurchargeOpt
                            .map(RoomSurcharge::getPriceModifier).orElse(BigDecimal.ZERO);
                    BigDecimal oldChildFee = multiplyByNightsIfPositive(oldChildFeePerNight, booking);

                    if (oldChildFee.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal afterRefund = detail.getExtraSurcharge() != null
                                ? detail.getExtraSurcharge().subtract(oldChildFee)
                                : BigDecimal.ZERO;
                        detail.setExtraSurcharge(afterRefund.max(BigDecimal.ZERO));
                        booking.setTotalPrice(booking.getTotalPrice().subtract(oldChildFee));
                        bookingRepository.save(booking);
                        roomBookingDetailRepository.save(detail);
                        log.info("[UC16-UPGRADE] Reversed CHILD surcharge {} VND for dep={}", oldChildFee,
                                saved.getId());
                    }

                    // Bước 2: Tính phụ thu người lớn mới (chỉ tính phần vượt quá quota paidAdults)
                    BigDecimal newAdultFee = applyGuestCountAndCalculateSurcharge(detail, booking, age,
                            detail.getCategory());
                    persistDetailAndBookingIfSurcharge(detail, booking, newAdultFee, saved.getId());

                    log.info("[UC16-UPGRADE] CHILD→ADULT upgrade: dep={}, netFee={}",
                            saved.getId(), newAdultFee.subtract(oldChildFee));
                }
            }
        }

        saveOrUpdateRoomGuest(saved, dto, detail, age);
    }

    /**
     * Tính tuổi từ ngày sinh. Trả về 18 nếu ngày sinh null (mặc định người lớn).
     */
    private int calculateAge(LocalDate birthDate) {
        return birthDate != null ? Period.between(birthDate, LocalDate.now()).getYears() : ADULT_AGE_THRESHOLD;
    }

    /**
     * Cập nhật số lượng người trong detail, kiểm tra capacity, và tính extra fee.
     * 
     * @return BigDecimal extra fee (có thể là ZERO nếu không có phụ thu).
     */
    private BigDecimal applyGuestCountAndCalculateSurcharge(
            RoomBookingDetail detail, RoomBooking booking, int age, com.kawai.models.RoomCategory category) {

        int maxAdults = category.getMaxAdults() != null ? category.getMaxAdults() : category.getCapacity();
        int maxChildren = category.getMaxChildren() != null ? category.getMaxChildren() : 2;

        int paidAdults = detail.getNumberOfAdults() != null ? detail.getNumberOfAdults() : 0;
        int paidChildren = detail.getNumberOfChildren() != null ? detail.getNumberOfChildren() : 0;

        // Đếm số lượng khách thực tế đã được xếp vào phòng này
        long checkedInAdults = roomGuestRepository.findByRoomBookingDetailId(detail.getId()).stream()
                .filter(rg -> GUEST_TYPE_ADULT.equals(rg.getGuestType()))
                .count();
        long checkedInChildren = roomGuestRepository.findByRoomBookingDetailId(detail.getId()).stream()
                .filter(rg -> GUEST_TYPE_CHILD.equals(rg.getGuestType()))
                .count();

        boolean isAdult = age >= ADULT_AGE_THRESHOLD;
        BigDecimal extraFee = BigDecimal.ZERO;

        if (isAdult) {
            long newAdultsCount = checkedInAdults + 1;
            if (newAdultsCount > maxAdults) {
                throw new BusinessException("MOD2-020",
                        "Số lượng khách vượt quá sức chứa tối đa của phòng. Tối đa: " + maxAdults + " người lớn.");
            }
            if (newAdultsCount > paidAdults) {
                if (category.getExtraAdultSurcharge() != null) {
                    extraFee = category.getExtraAdultSurcharge();
                }
                detail.setNumberOfAdults((int) newAdultsCount);
            }
        } else {
            long newChildrenCount = checkedInChildren + 1;
            if (newChildrenCount > maxChildren) {
                throw new BusinessException("MOD2-021",
                        "Số lượng khách vượt quá sức chứa tối đa của phòng. Tối đa: " + maxChildren + " trẻ em.");
            }
            if (newChildrenCount > paidChildren) {
                Optional<RoomSurcharge> surchargeOpt = roomSurchargeRepository.findSurchargeForAge(category, age);
                extraFee = surchargeOpt.map(RoomSurcharge::getPriceModifier).orElse(BigDecimal.ZERO);
                detail.setNumberOfChildren((int) newChildrenCount);
            }
        }

        return multiplyByNightsIfPositive(extraFee, booking);
    }

    /** Nhân extra fee với số đêm nếu fee > 0 và booking có ngày hợp lệ. */
    private BigDecimal multiplyByNightsIfPositive(BigDecimal extraFee, RoomBooking booking) {
        if (extraFee.compareTo(BigDecimal.ZERO) <= 0)
            return extraFee;
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null)
            return extraFee;

        long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        return nights > 0 ? extraFee.multiply(BigDecimal.valueOf(nights)) : extraFee;
    }

    /** Lưu detail và cập nhật tổng tiền booking nếu có phụ thu. */
    private void persistDetailAndBookingIfSurcharge(
            RoomBookingDetail detail, RoomBooking booking, BigDecimal extraFee, Long dependentId) {

        if (extraFee.compareTo(BigDecimal.ZERO) > 0) {
            if (detail.getExtraSurcharge() == null)
                detail.setExtraSurcharge(BigDecimal.ZERO);
            detail.setExtraSurcharge(detail.getExtraSurcharge().add(extraFee));

            booking.setTotalPrice(booking.getTotalPrice().add(extraFee));
            bookingRepository.save(booking);

            log.info("Extra surcharge applied: {} VND for dependent {} in detail {}",
                    extraFee, dependentId, detail.getId());
        }
        roomBookingDetailRepository.save(detail);
    }

    /** Tạo hoặc cập nhật RoomGuest entry liên kết Dependent với phòng. */
    private void saveOrUpdateRoomGuest(Dependent saved, DependentRegistrationDTO dto, RoomBookingDetail detail,
            int age) {
        RoomGuest rg = (dto.getDependentId() != null)
                ? roomGuestRepository.findFirstByDependentId(saved.getId()).orElse(new RoomGuest())
                : new RoomGuest();

        rg.setRoomBookingDetail(detail);
        rg.setDependent(saved);
        rg.setCustomer(null);
        rg.setIsPrimaryContact(Boolean.TRUE.equals(dto.getIsPrimaryContact()));
        rg.setGuestType(age < ADULT_AGE_THRESHOLD ? GUEST_TYPE_CHILD : GUEST_TYPE_ADULT);

        roomGuestRepository.saveAndFlush(rg);
    }

    /** Build DependentResponseDTO từ kết quả persist và request DTO. */
    private DependentResponseDTO buildResponseDTO(Dependent saved, DependentRegistrationDTO dto) {
        DependentResponseDTO response = new DependentResponseDTO();
        response.setDependentId(saved.getId());
        response.setFullName(dto.getFullName());
        response.setDateOfBirth(dto.getDateOfBirth());
        response.setGender(saved.getGender());
        response.setStatus(STATUS_REGISTERED);
        return response;
    }

    /**
     * Map RoomGuest entity sang DependentResponseDTO (dùng trong
     * getGuestListByBooking).
     */
    private DependentResponseDTO mapGuestToResponseDTO(RoomGuest guest, RoomBookingDetail detail) {
        DependentResponseDTO dto = new DependentResponseDTO();
        dto.setRoomBookingDetailId(detail.getId());

        if (guest.getDependent() != null) {
            dto.setDependentId(guest.getDependent().getId());
            dto.setFullName(guest.getDependent().getDependentName());
            dto.setDateOfBirth(guest.getDependent().getBirthDate());
            dto.setGender(guest.getDependent().getGender());
            String cccdEnc = guest.getDependent().getCccdPassportEncrypted();
            if (cccdEnc != null && !cccdEnc.isBlank()) {
                try {
                    dto.setCccd(com.kawai.utils.EncryptionUtils.decrypt(cccdEnc));
                } catch (Throwable e) {
                    dto.setCccd(cccdEnc);
                }
            }
        } else if (guest.getCustomer() != null) {
            dto.setDependentId(null); // Explicit null indicating this is an upgraded Customer, not a Dependent
            dto.setFullName(guest.getCustomer().getFullName());
            dto.setDateOfBirth(guest.getCustomer().getBirthDate());
            dto.setGender(guest.getCustomer().getGender());
            String cccdEnc = guest.getCustomer().getCccdPassportEncrypted();
            if (cccdEnc != null && !cccdEnc.isBlank()) {
                try {
                    dto.setCccd(com.kawai.utils.EncryptionUtils.decrypt(cccdEnc));
                } catch (Throwable e) {
                    dto.setCccd(cccdEnc);
                }
            }
        }
        dto.setIsPrimaryContact(guest.getIsPrimaryContact());
        if (detail.getRoom() != null) {
            dto.setAssignedRoom(detail.getRoom().getRoomNumber());
        }
        return dto;
    }

    /**
     * @deprecated Dùng {@link #validateCccdIfPresent(String)} thay thế.
     *             Phương thức này giữ lại để tránh breaking change với code legacy.
     */
    @Deprecated(since = "refactor-2026-06-30", forRemoval = true)
    private void validateCccdFormat(String cccd) {
        validateCccdIfPresent(cccd);
    }
}
