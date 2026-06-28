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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DependentServiceImpl — UC16: Register Accompanying Guests
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * TDD Phase: 🔴 RED → 🟢 GREEN skeleton
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

    /** Trạng thái booking được phép thêm dependent (Invariant §6.5 EDS). */
    private static final java.util.Set<String> ACTIVE_STATUSES = java.util.Set.of("Confirmed", "Checked_In");

    private final DependentRepository dependentRepository;
    private final RoomBookingRepository bookingRepository;
    private final EncryptionService encryptionService;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomGuestRepository roomGuestRepository;
    private final RoomSurchargeRepository roomSurchargeRepository;

    public DependentServiceImpl(DependentRepository dependentRepository,
            RoomBookingRepository bookingRepository,
            EncryptionService encryptionService,
            RoomBookingDetailRepository roomBookingDetailRepository,
            RoomGuestRepository roomGuestRepository,
            RoomSurchargeRepository roomSurchargeRepository) {
        this.dependentRepository = dependentRepository;
        this.bookingRepository = bookingRepository;
        this.encryptionService = encryptionService;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomGuestRepository = roomGuestRepository;
        this.roomSurchargeRepository = roomSurchargeRepository;
    }

    // ══════════════════════════════════════════════════════════════════════
    // registerDependent()
    // ══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DependentResponseDTO registerDependent(Long bookingId, DependentRegistrationDTO dto) {

        // 1. Validate CCCD format (BR-SYS-01, MOD2-017) nếu có
        if (dto.getCccd() != null && !dto.getCccd().trim().isEmpty()) {
            validateCccdFormat(dto.getCccd());
        }

        // Validate Date of Birth (không được ở tương lai)
        if (dto.getDateOfBirth() != null && dto.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new BusinessException("MOD2-001", "Invalid Date of Birth: Cannot be in the future [MOD2-001]");
        }

        // 2. Tìm booking (MOD2-003)
        RoomBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("MOD2-003",
                        "Booking not found with ID: " + bookingId + " [MOD2-003]"));

        // 3. Kiểm tra trạng thái booking (MOD2-015)
        if (!ACTIVE_STATUSES.contains(booking.getBookingStatus())) {
            throw new BusinessException("MOD2-015",
                    "Reservation is not active (must be Confirmed or Checked_In). " +
                            "Current status: " + booking.getBookingStatus() + " [MOD2-015]");
        }

        // 4. Mã hoá CCCD AES-256 (BR-SYS-01 — Nghị định 13/2023)
        String cccdEncrypted = null;
        if (dto.getCccd() != null && !dto.getCccd().trim().isEmpty()) {
            try {
                cccdEncrypted = encryptionService.encrypt(dto.getCccd());
            } catch (Exception e) {
                log.error("[UC16] AES-256 encryption failed for dependent registration: {}", e.getMessage());
                throw new BusinessException("MOD2-005", "Internal error during CCCD encryption [MOD2-005]");
            }
        }

        // 5. Kiểm tra trùng lặp CCCD trong cùng booking (ADR-002, MOD2-016)
        if (cccdEncrypted != null) {
            int duplicateCount = dependentRepository.countDuplicateInBooking(bookingId, cccdEncrypted);
            // Ignore duplicate check if updating the SAME dependent
            if (dto.getDependentId() == null && duplicateCount > 0) {
                throw new BusinessException("MOD2-016",
                        "Guest already registered under this reservation [MOD2-016]");
            }
        }

        // 6. Tạo hoặc Cập nhật Dependent entity và lưu vào DB
        Dependent dependent;
        if (dto.getDependentId() != null) {
            dependent = dependentRepository.findById(dto.getDependentId())
                    .orElseThrow(() -> new BusinessException("MOD2-018",
                            "Dependent not found with ID: " + dto.getDependentId()));
            // Không được đổi customer của dependent
        } else {
            dependent = new Dependent();
            dependent.setCustomer(booking.getCustomer());
        }

        dependent.setDependentName(dto.getFullName() != null && !dto.getFullName().isBlank() ? dto.getFullName().trim() : "Khách đi kèm");
        dependent.setBirthDate(dto.getDateOfBirth() != null ? dto.getDateOfBirth() : java.time.LocalDate.now().minusYears(18).withDayOfYear(1));
        dependent.setGender(dto.getGender() != null ? dto.getGender() : "Khác");
        dependent.setCccdPassportEncrypted(cccdEncrypted); // Lưu đã mã hoá, không phải plaintext

        Dependent saved = dependentRepository.save(dependent);

        // 6.5. Nếu là Khách mới thêm tại lễ tân -> Liên kết vào RoomBookingDetail và
        // Tính phụ thu
        if (dto.getRoomBookingDetailId() != null) {
            RoomBookingDetail detail = roomBookingDetailRepository.findById(dto.getRoomBookingDetailId())
                    .orElseThrow(() -> new BusinessException("MOD2-019", "RoomBookingDetail not found"));

            com.kawai.models.RoomCategory category = detail.getCategory();
            int maxAdults = category.getMaxAdults() != null ? category.getMaxAdults() : category.getCapacity();
            int maxChildren = category.getMaxChildren() != null ? category.getMaxChildren() : 2;
            int baseAdults = category.getBaseAdults() != null ? category.getBaseAdults() : category.getCapacity();
            int baseChildren = category.getBaseChildren() != null ? category.getBaseChildren() : 0;

            int currentAdults = detail.getNumberOfAdults() != null ? detail.getNumberOfAdults() : 0;
            int currentChildren = detail.getNumberOfChildren() != null ? detail.getNumberOfChildren() : 0;
            int age = 18;
            if (saved.getBirthDate() != null) {
                age = Period.between(saved.getBirthDate(), LocalDate.now()).getYears();
            }

            boolean isAdult = age >= 18; // 18 is ADULT_AGE_THRESHOLD
            java.math.BigDecimal extraFee = java.math.BigDecimal.ZERO;

            if (dto.getDependentId() == null) {
                if (isAdult) {
                    currentAdults++;
                    if (currentAdults > maxAdults) {
                        throw new BusinessException("MOD2-020",
                                "Number of guests exceeds maximum room capacity. Max adults: " + maxAdults);
                    }
                    if (currentAdults > baseAdults && category.getExtraAdultSurcharge() != null) {
                        extraFee = category.getExtraAdultSurcharge();
                    }
                } else {
                    currentChildren++;
                    if (currentChildren > maxChildren) {
                        throw new BusinessException("MOD2-021",
                                "Number of guests exceeds maximum room capacity. Max children: " + maxChildren);
                    }
                    if (currentChildren > baseChildren) {
                        java.util.Optional<RoomSurcharge> surchargeOpt = roomSurchargeRepository
                                .findSurchargeForAge(category, age);
                        if (surchargeOpt.isPresent()) {
                            extraFee = surchargeOpt.get().getPriceModifier();
                        }
                    }
                }

                if (extraFee.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                        long nights = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(),
                                booking.getCheckOutDate());
                        if (nights > 0) {
                            extraFee = extraFee.multiply(java.math.BigDecimal.valueOf(nights));
                        }
                    }
                }

                // Cập nhật lại số lượng khách thực tế trong phòng
                detail.setNumberOfAdults(currentAdults);
                detail.setNumberOfChildren(currentChildren);

                // Cộng phụ thu vào detail và booking
                if (extraFee.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    if (detail.getExtraSurcharge() == null) {
                        detail.setExtraSurcharge(java.math.BigDecimal.ZERO);
                    }
                    detail.setExtraSurcharge(detail.getExtraSurcharge().add(extraFee));

                    booking.setTotalPrice(booking.getTotalPrice().add(extraFee));
                    bookingRepository.save(booking);

                    log.info("[UC16] Added extra fee: {} for dependent {} in detail {}", extraFee, saved.getId(),
                            detail.getId());
                }

                roomBookingDetailRepository.save(detail);
            }

            // Tạo hoặc cập nhật RoomGuest
            RoomGuest rg = null;
            if (dto.getDependentId() != null) {
                rg = roomGuestRepository.findByDependentId(saved.getId()).orElse(null);
            }
            if (rg == null) {
                rg = new RoomGuest();
            }
            rg.setRoomBookingDetail(detail);
            rg.setDependent(saved);
            rg.setGuestType(age < 12 ? "CHILD" : "ADULT");
            rg.setIsPrimaryContact(dto.getIsPrimaryContact() != null ? dto.getIsPrimaryContact() : false);
            roomGuestRepository.saveAndFlush(rg);
        }

        log.info("[UC16] Dependent registered: bookingId={}, dependentId={}", bookingId, saved.getId());

        // 7. Build response DTO
        DependentResponseDTO response = new DependentResponseDTO();
        response.setDependentId(saved.getId());
        response.setFullName(dto.getFullName());
        response.setDateOfBirth(dto.getDateOfBirth());

        return response;
    }

    // ══════════════════════════════════════════════════════════════════════
    // getGuestListByBooking()
    // ══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<DependentResponseDTO> getGuestListByBooking(Long bookingId) {
        RoomBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("MOD2-003",
                        "Booking not found with ID: " + bookingId + " [MOD2-003]"));

        List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(bookingId);
        List<DependentResponseDTO> result = new java.util.ArrayList<>();

        for (RoomBookingDetail detail : details) {
            List<RoomGuest> guests = roomGuestRepository.findByRoomBookingDetailId(detail.getId());
            for (RoomGuest guest : guests) {
                // Bỏ qua Chủ đơn (Customer) vì đã hiển thị ở phần thông tin chung.
                if (guest.getCustomer() != null) {
                    continue;
                }

                DependentResponseDTO dto = new DependentResponseDTO();
                if (guest.getDependent() != null) {
                    dto.setDependentId(guest.getDependent().getId());
                    dto.setFullName(guest.getDependent().getDependentName());
                    dto.setDateOfBirth(guest.getDependent().getBirthDate());
                } else {
                    dto.setDependentId(null);
                    dto.setFullName(null);
                    dto.setDateOfBirth(null);
                }
                dto.setIsPrimaryContact(guest.getIsPrimaryContact());
                if (detail.getRoom() != null) {
                    dto.setAssignedRoom(detail.getRoom().getRoomNumber());
                }
                result.add(dto);
            }
        }

        return result;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Validate định dạng CCCD/Hộ chiếu (BR-SYS-01, MOD2-017).
     * Sử dụng ValidationUtils.
     */
    private void validateCccdFormat(String cccd) {
        if (cccd == null || cccd.isBlank() || !com.kawai.utils.ValidationUtils.isValidDocument(cccd)) {
            throw new BusinessException("MOD2-017",
                    "Invalid identification document: Must be a 12-digit CCCD or a valid Passport [MOD2-017]");
        }
    }
}
