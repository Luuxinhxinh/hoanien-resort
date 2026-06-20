package com.kawai.services.impl;

import com.kawai.dto.DependentRegistrationDTO;
import com.kawai.dto.DependentResponseDTO;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.Dependent;
import com.kawai.models.RoomBooking;
import com.kawai.repositories.DependentRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.services.interfaces.DependentService;
import com.kawai.services.interfaces.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * ADR-002   : Kiểm tra trùng lặp theo booking_id + cccd_encrypted
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

    /** Độ dài CCCD hợp lệ: 9 ký tự (CMND cũ) hoặc 12 ký tự (CCCD mới). */
    private static final int CCCD_MIN_LENGTH = 9;
    private static final int CCCD_MAX_LENGTH = 12;

    /** Trạng thái booking được phép thêm dependent (Invariant §6.5 EDS). */
    private static final java.util.Set<String> ACTIVE_STATUSES = java.util.Set.of("Confirmed", "Checked_In");

    private final DependentRepository dependentRepository;
    private final RoomBookingRepository bookingRepository;
    private final EncryptionService encryptionService;

    public DependentServiceImpl(DependentRepository dependentRepository,
                                RoomBookingRepository bookingRepository,
                                EncryptionService encryptionService) {
        this.dependentRepository = dependentRepository;
        this.bookingRepository = bookingRepository;
        this.encryptionService = encryptionService;
    }

    // ══════════════════════════════════════════════════════════════════════
    // registerDependent()
    // ══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DependentResponseDTO registerDependent(Long bookingId, DependentRegistrationDTO dto) {

        // 1. Validate CCCD format (BR-SYS-01, MOD2-017)
        validateCccdFormat(dto.getCccd());

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
        String cccdEncrypted;
        try {
            cccdEncrypted = encryptionService.encrypt(dto.getCccd());
        } catch (Exception e) {
            log.error("[UC16] AES-256 encryption failed for dependent registration: {}", e.getMessage());
            throw new BusinessException("MOD2-005", "Internal error during CCCD encryption [MOD2-005]");
        }

        // 5. Kiểm tra trùng lặp CCCD trong cùng booking (ADR-002, MOD2-016)
        int duplicateCount = dependentRepository.countDuplicateInBooking(bookingId, cccdEncrypted);
        if (duplicateCount > 0) {
            throw new BusinessException("MOD2-016",
                    "Guest already registered under this reservation [MOD2-016]");
        }

        // 6. Tạo Dependent entity và lưu vào DB
        Dependent dependent = new Dependent();
        dependent.setDependentName(dto.getFullName());
        dependent.setBirthDate(dto.getDateOfBirth());
        dependent.setGender(dto.getGender());
        dependent.setCccdPassportEncrypted(cccdEncrypted); // Lưu đã mã hoá, không phải plaintext
        dependent.setCustomer(booking.getCustomer());

        Dependent saved = dependentRepository.save(dependent);

        log.info("[UC16] Dependent registered: bookingId={}, dependentId={}", bookingId, saved.getId());

        // 7. Build response DTO
        DependentResponseDTO response = new DependentResponseDTO();
        response.setDependentId(saved.getId());
        response.setFullName(dto.getFullName());
        response.setDateOfBirth(dto.getDateOfBirth());
        response.setStatus("REGISTERED");

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

        List<Dependent> dependents = dependentRepository.findByCustomer(booking.getCustomer());

        return dependents.stream().map(d -> {
            DependentResponseDTO dto = new DependentResponseDTO();
            dto.setDependentId(d.getId());
            dto.setFullName(d.getDependentName());
            dto.setDateOfBirth(d.getBirthDate());
            dto.setStatus("REGISTERED");
            return dto;
        }).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private Helpers
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Validate định dạng CCCD/Hộ chiếu (BR-SYS-01, MOD2-017).
     * CCCD hợp lệ: 9 ký tự (CMND cũ) hoặc 12 ký tự (CCCD chip mới).
     */
    private void validateCccdFormat(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            throw new BusinessException("MOD2-017",
                    "Invalid identification document: CCCD/Passport is required [MOD2-017]");
        }
        int len = cccd.trim().length();
        if (len < CCCD_MIN_LENGTH || len > CCCD_MAX_LENGTH) {
            throw new BusinessException("MOD2-017",
                    "Invalid identification document: CCCD must be " + CCCD_MIN_LENGTH + "-" + CCCD_MAX_LENGTH
                    + " characters. Got: " + len + " [MOD2-017]");
        }
    }
}
