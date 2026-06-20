package com.kawai.receptionist;

import com.kawai.exceptions.BusinessException;
import com.kawai.models.Dependent;
import com.kawai.models.RoomBooking;
import com.kawai.repositories.DependentRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.services.interfaces.DependentService;
import com.kawai.services.interfaces.EncryptionService;
import com.kawai.dto.DependentRegistrationDTO;
import com.kawai.dto.DependentResponseDTO;
import com.kawai.services.impl.DependentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC16: Register Accompanying Guests (DependentService)
 * MODULE 2: Quản lý Đặt phòng & Lễ tân
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Standard   : ISO/IEC/IEEE 29119-3:2021
 * TDD Phase  : 🔴 RED — Các test case phải FAIL trước khi implement.
 * Document   : KAWAI-TDD-MOD2-UC16-001 v1.0 (2026-06-18)
 * Test File  : DependentServiceUC16Test.java
 *
 * Ánh xạ test case (TDD_UC16_SPEC.md — §4):
 * ┌─────────────┬─────────────────────────────────────────────┬──────────┐
 * │ TC ID       │ Kịch bản                                    │ Severity │
 * ├─────────────┼─────────────────────────────────────────────┼──────────┤
 * │ TC-UC16-001 │ Đăng ký dependent mới thành công           │ CRITICAL │
 * │ TC-UC16-002 │ Booking Cancelled → từ chối (MOD2-015)     │ HIGH     │
 * │ TC-UC16-003 │ Trùng CCCD → DuplicateException (MOD2-016) │ HIGH     │
 * │ TC-UC16-004 │ CCCD không hợp lệ → InvalidId (MOD2-017)  │ MEDIUM   │
 * │ TC-UC16-005 │ Booking Checked_In → thành công (AF-02)    │ HIGH     │
 * │ TC-UC16-009 │ PII CCCD mã hoá AES-256, không plaintext   │ CRITICAL │
 * │ TC-UC16-010 │ bookingId không tồn tại → 404 (MOD2-003)  │ HIGH     │
 * └─────────────┴─────────────────────────────────────────────┴──────────┘
 *
 * Business Rules kiểm thử:
 * BR-SYS-01 : CCCD/Hộ chiếu phải mã hoá AES-256 trước khi lưu DB
 * BR-FO-07  : Chỉ dependent đã đăng ký mới được cấp quyền dịch vụ (UC17)
 * ADR-002   : Kiểm tra trùng lặp Dependent theo booking_id + cccd_encrypted
 *
 * Error Codes:
 * MOD2-003 : Booking not found (404)
 * MOD2-015 : Reservation is not active (400)
 * MOD2-016 : Duplicate guest registration (409)
 * MOD2-017 : Invalid identification document (422)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC16 — Register Accompanying Guests (DependentService) | TDD 🔴 RED")
class DependentServiceUC16Test {

    // ── SUT ───────────────────────────────────────────────────────────────────
    @InjectMocks
    private DependentServiceImpl dependentService;

    // ── Mocks ─────────────────────────────────────────────────────────────────
    @Mock
    private DependentRepository dependentRepository;

    @Mock
    private RoomBookingRepository bookingRepository;

    @Mock
    private EncryptionService encryptionService;

    // ── Test Fixtures ─────────────────────────────────────────────────────────
    private static final Long   BOOKING_ID_CONFIRMED   = 12L;
    private static final Long   BOOKING_ID_CANCELLED   = 99L;
    private static final Long   BOOKING_ID_CHECKED_IN  = 55L;
    private static final Long   BOOKING_ID_NOT_FOUND   = 9999L;
    private static final String VALID_CCCD             = "034095012345"; // 12 ký tự hợp lệ
    private static final String VALID_CCCD_2           = "034095099999"; // 12 ký tự hợp lệ
    private static final String INVALID_CCCD_SHORT     = "123";          // < 9 ký tự — không hợp lệ
    private static final String ENCRYPTED_CCCD         = "ENCRYPTED_AES256_VALUE";
    private static final String ENCRYPTED_CCCD_2       = "ENCRYPTED_AES256_VALUE_2";

    /** Helper: tạo DependentRegistrationDTO chuẩn với CCCD hợp lệ. */
    private DependentRegistrationDTO buildDto(String cccd) {
        DependentRegistrationDTO dto = new DependentRegistrationDTO();
        dto.setFullName("Nguyen Van B");
        dto.setDateOfBirth(LocalDate.of(1995, 8, 20));
        dto.setCccd(cccd);
        dto.setGender("Nam");
        dto.setContactInfo("0901234567");
        return dto;
    }

    /** Helper: tạo RoomBooking mock với trạng thái cho trước. */
    private RoomBooking buildBooking(Long id, String status) {
        RoomBooking booking = new RoomBooking();
        booking.setId(id);
        booking.setBookingStatus(status);
        return booking;
    }

    @BeforeEach
    void setUp() {
        // Dependent save mặc định: trả lại chính đối tượng đó + gán id=7
        lenient().when(dependentRepository.save(any(Dependent.class))).thenAnswer(invocation -> {
            Dependent d = invocation.getArgument(0);
            if (d.getId() == null) {
                d.setId(7L);
            }
            return d;
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-UC16-001 | CRITICAL | Đăng ký dependent mới thành công
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC-UC16-001 — Happy path: đăng ký thành công, CCCD được mã hoá, save được gọi.
     *
     * Spec (TDD_UC16_SPEC §4 TC-UC16-001):
     *   Given booking BK-12 trạng thái Confirmed, không có dependent trùng CCCD
     *   When registerDependent(12, dto{fullName:"Nguyen Van B", cccd:"034095012345"})
     *   Then trả về DependentResponseDTO với dependentId=7, status="REGISTERED"
     *   And dependentRepository.save() được gọi đúng 1 lần
     *   And encryptionService.encrypt() được gọi đúng 1 lần
     *
     * 🔴 RED: DependentService và DependentServiceImpl chưa tồn tại → COMPILE ERROR
     */
    @Test
    @DisplayName("TC-UC16-001 | CRITICAL | Đăng ký dependent mới → status=REGISTERED, id=7")
    void TC_UC16_001_registerDependent_happyPath_returnsRegisteredDTO() throws Exception {
        // Arrange
        when(bookingRepository.findById(BOOKING_ID_CONFIRMED))
                .thenReturn(Optional.of(buildBooking(BOOKING_ID_CONFIRMED, "Confirmed")));
        when(encryptionService.encrypt(VALID_CCCD)).thenReturn(ENCRYPTED_CCCD);
        when(dependentRepository.countDuplicateInBooking(BOOKING_ID_CONFIRMED, ENCRYPTED_CCCD))
                .thenReturn(0);

        DependentRegistrationDTO dto = buildDto(VALID_CCCD);

        // Act
        DependentResponseDTO result = dependentService.registerDependent(BOOKING_ID_CONFIRMED, dto);

        // Assert — response hợp lệ
        assertNotNull(result, "Response không được null");
        assertNotNull(result.getDependentId(), "dependentId phải được sinh ra");
        assertEquals(7L, result.getDependentId(), "dependentId phải bằng 7");
        assertEquals("Nguyen Van B", result.getFullName(), "fullName phải khớp");
        assertEquals("REGISTERED", result.getStatus(), "status phải là REGISTERED");

        // Verify interactions
        verify(dependentRepository, times(1)).save(any(Dependent.class));
        verify(encryptionService, times(1)).encrypt(VALID_CCCD);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-UC16-002 | HIGH | Booking trạng thái Cancelled → từ chối (MOD2-015)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC-UC16-002 — Booking đã bị huỷ, không cho phép thêm dependent.
     *
     * Spec (TDD_UC16_SPEC §4 TC-UC16-002):
     *   Given booking BK-99 trạng thái Cancelled
     *   When registerDependent(99, dto) được gọi
     *   Then ném BusinessException với errorCode "MOD2-015"
     *   And dependentRepository.save() KHÔNG được gọi
     *
     * 🔴 RED: DependentServiceImpl chưa implement kiểm tra status → FAIL
     */
    @Test
    @DisplayName("TC-UC16-002 | HIGH | Booking Cancelled → BusinessException [MOD2-015]")
    void TC_UC16_002_registerDependent_cancelledBooking_throwsMOD2015() {
        // Arrange
        when(bookingRepository.findById(BOOKING_ID_CANCELLED))
                .thenReturn(Optional.of(buildBooking(BOOKING_ID_CANCELLED, "Cancelled")));

        DependentRegistrationDTO dto = buildDto(VALID_CCCD);

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> dependentService.registerDependent(BOOKING_ID_CANCELLED, dto),
                "Booking Cancelled phải ném BusinessException"
        );

        assertTrue(
                ex.getMessage().contains("MOD2-015") || ex.getMessage().toLowerCase().contains("not active")
                        || ex.getMessage().toLowerCase().contains("reservation"),
                "Message phải chứa 'MOD2-015' hoặc 'not active'. Thực tế: " + ex.getMessage()
        );
        assertEquals("MOD2-015", ex.getErrorCode(),
                "errorCode phải là MOD2-015 (Reservation is not active)");

        // Verify: save KHÔNG được gọi
        verify(dependentRepository, never()).save(any());
    }

    /**
     * TC-UC16-002b — Booking trạng thái Checked_Out cũng phải bị từ chối.
     *
     * 🔴 RED: extension của TC-UC16-002 theo Business Rule invariant §6.5
     */
    @Test
    @DisplayName("TC-UC16-002b | HIGH | Booking Checked_Out → BusinessException [MOD2-015]")
    void TC_UC16_002b_registerDependent_checkedOutBooking_throwsMOD2015() {
        // Arrange
        when(bookingRepository.findById(BOOKING_ID_CANCELLED))
                .thenReturn(Optional.of(buildBooking(BOOKING_ID_CANCELLED, "Checked_Out")));

        DependentRegistrationDTO dto = buildDto(VALID_CCCD);

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> dependentService.registerDependent(BOOKING_ID_CANCELLED, dto),
                "Booking Checked_Out phải ném BusinessException"
        );
        assertEquals("MOD2-015", ex.getErrorCode(),
                "errorCode phải là MOD2-015 dù status là Checked_Out hay Cancelled");
        verify(dependentRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-UC16-003 | HIGH | Trùng lặp CCCD → DuplicateException (MOD2-016)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC-UC16-003 — CCCD đã tồn tại trong booking này, không cho phép đăng ký lại.
     *
     * Spec (TDD_UC16_SPEC §4 TC-UC16-003 | ADR-002):
     *   Given booking BK-12 đã có dependent CCCD "034095012345" (count=1)
     *   When registerDependent(12, {cccd:"034095012345"}) lần 2
     *   Then ném BusinessException với errorCode "MOD2-016"
     *   And dependentRepository.save() KHÔNG được gọi
     *
     * 🔴 RED: Kiểm tra duplicate chưa được implement → FAIL
     */
    @Test
    @DisplayName("TC-UC16-003 | HIGH | Trùng CCCD trong booking → BusinessException [MOD2-016]")
    void TC_UC16_003_registerDependent_duplicateCccd_throwsMOD2016() throws Exception {
        // Arrange
        when(bookingRepository.findById(BOOKING_ID_CONFIRMED))
                .thenReturn(Optional.of(buildBooking(BOOKING_ID_CONFIRMED, "Confirmed")));
        when(encryptionService.encrypt(VALID_CCCD)).thenReturn(ENCRYPTED_CCCD);
        when(dependentRepository.countDuplicateInBooking(BOOKING_ID_CONFIRMED, ENCRYPTED_CCCD))
                .thenReturn(1); // đã tồn tại → duplicate

        DependentRegistrationDTO dto = buildDto(VALID_CCCD);

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> dependentService.registerDependent(BOOKING_ID_CONFIRMED, dto),
                "Duplicate CCCD phải ném BusinessException"
        );

        assertTrue(
                ex.getMessage().contains("MOD2-016") || ex.getMessage().toLowerCase().contains("already registered")
                        || ex.getMessage().toLowerCase().contains("duplicate"),
                "Message phải chứa 'MOD2-016' hoặc 'already registered'. Thực tế: " + ex.getMessage()
        );
        assertEquals("MOD2-016", ex.getErrorCode(),
                "errorCode phải là MOD2-016 (Guest already registered under this reservation)");

        // Verify: save KHÔNG được gọi
        verify(dependentRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-UC16-004 | MEDIUM | CCCD định dạng không hợp lệ (MOD2-017)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC-UC16-004 — CCCD chỉ 3 ký tự (không đủ 9-12 ký tự chuẩn VN).
     *
     * Spec (TDD_UC16_SPEC §4 TC-UC16-004):
     *   Given CCCD = "123" (3 ký tự — không hợp lệ)
     *   When registerDependent(12, dto{cccd:"123"})
     *   Then ném BusinessException với errorCode "MOD2-017"
     *   And dependentRepository.save() KHÔNG được gọi
     *
     * 🔴 RED: Validation CCCD format chưa implement → FAIL
     */
    @Test
    @DisplayName("TC-UC16-004 | MEDIUM | CCCD quá ngắn (3 ký tự) → BusinessException [MOD2-017]")
    void TC_UC16_004_registerDependent_invalidCccdFormat_throwsMOD2017() {
        // Arrange: CCCD chỉ 3 ký tự — validate format xảy ra TRƯỜC khi gọi repository
        // Nên không cần stub bookingRepository (Mockito strict mode báo lỗi nếu stub thừa)
        DependentRegistrationDTO dto = buildDto(INVALID_CCCD_SHORT); // "123"

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> dependentService.registerDependent(BOOKING_ID_CONFIRMED, dto),
                "CCCD không hợp lệ phải ném BusinessException"
        );

        assertTrue(
                ex.getMessage().contains("MOD2-017") || ex.getMessage().toLowerCase().contains("invalid identification")
                        || ex.getMessage().toLowerCase().contains("cccd"),
                "Message phải chứa 'MOD2-017' hoặc 'invalid identification'. Thực tế: " + ex.getMessage()
        );
        assertEquals("MOD2-017", ex.getErrorCode(),
                "errorCode phải là MOD2-017 (Invalid identification document)");

        // Verify: save KHÔNG được gọi
        verify(dependentRepository, never()).save(any());
    }

    /**
     * TC-UC16-004b — CCCD null cũng phải bị từ chối.
     */
    @Test
    @DisplayName("TC-UC16-004b | MEDIUM | CCCD null → BusinessException [MOD2-017]")
    void TC_UC16_004b_registerDependent_nullCccd_throwsMOD2017() {
        // Arrange: null CCCD — validate format xảy ra TRƯỜC khi gọi repository
        DependentRegistrationDTO dto = buildDto(null); // null

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> dependentService.registerDependent(BOOKING_ID_CONFIRMED, dto),
                "CCCD null phải ném BusinessException"
        );
        assertEquals("MOD2-017", ex.getErrorCode(), "errorCode phải là MOD2-017");
        verify(dependentRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-UC16-005 | HIGH | Booking Checked_In → đăng ký thành công (AF-02)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC-UC16-005 — Booking ở trạng thái Checked_In vẫn được phép thêm dependent.
     *
     * Spec (TDD_UC16_SPEC §4 TC-UC16-005 | SRS §2.1.16 AF-02):
     *   Given booking BK-55 trạng thái Checked_In
     *   When registerDependent(55, dto{cccd:"034095099999"})
     *   Then đăng ký thành công, không ném exception
     *
     * 🔴 RED: DependentServiceImpl chưa cho phép Checked_In → FAIL
     */
    @Test
    @DisplayName("TC-UC16-005 | HIGH | Booking Checked_In → đăng ký thành công (SRS AF-02)")
    void TC_UC16_005_registerDependent_checkedInBooking_success() throws Exception {
        // Arrange
        when(bookingRepository.findById(BOOKING_ID_CHECKED_IN))
                .thenReturn(Optional.of(buildBooking(BOOKING_ID_CHECKED_IN, "Checked_In")));
        when(encryptionService.encrypt(VALID_CCCD_2)).thenReturn(ENCRYPTED_CCCD_2);
        when(dependentRepository.countDuplicateInBooking(BOOKING_ID_CHECKED_IN, ENCRYPTED_CCCD_2))
                .thenReturn(0);

        DependentRegistrationDTO dto = buildDto(VALID_CCCD_2);

        // Act
        DependentResponseDTO result = dependentService.registerDependent(BOOKING_ID_CHECKED_IN, dto);

        // Assert — đăng ký thành công
        assertNotNull(result, "Response không được null khi booking là Checked_In");
        assertEquals("REGISTERED", result.getStatus(),
                "Booking Checked_In phải được đăng ký thành công như Confirmed (AF-02)");

        // Verify save được gọi
        verify(dependentRepository, times(1)).save(any(Dependent.class));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-UC16-009 | CRITICAL | PII CCCD phải mã hoá AES-256, không lưu plaintext
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC-UC16-009 — Nghị định 13/2023/NĐ-CP BR-SYS-01: CCCD lưu DB phải là chuỗi đã mã hoá.
     *
     * Spec (TDD_UC16_SPEC §4 TC-UC16-009):
     *   Given encryptionService.encrypt("034095012345") → "ENCRYPTED_AES256_VALUE"
     *   When registerDependent(12, dto) hoàn tất
     *   Then dependent được save với cccdPassportEncrypted = "ENCRYPTED_AES256_VALUE"
     *   And cccdPassportEncrypted != "034095012345" (không phải plaintext)
     *
     * 🔴 RED: DependentServiceImpl chưa gọi encrypt trước khi save → FAIL
     */
    @Test
    @DisplayName("TC-UC16-009 | CRITICAL | CCCD phải mã hoá AES-256 trước khi lưu DB (BR-SYS-01)")
    void TC_UC16_009_registerDependent_cccdMustBeEncrypted_notPlaintext() throws Exception {
        // Arrange
        when(bookingRepository.findById(BOOKING_ID_CONFIRMED))
                .thenReturn(Optional.of(buildBooking(BOOKING_ID_CONFIRMED, "Confirmed")));
        when(encryptionService.encrypt(VALID_CCCD)).thenReturn(ENCRYPTED_CCCD);
        when(dependentRepository.countDuplicateInBooking(BOOKING_ID_CONFIRMED, ENCRYPTED_CCCD))
                .thenReturn(0);

        DependentRegistrationDTO dto = buildDto(VALID_CCCD);

        // Act
        dependentService.registerDependent(BOOKING_ID_CONFIRMED, dto);

        // Capture argument truyền vào save()
        ArgumentCaptor<Dependent> captor = ArgumentCaptor.forClass(Dependent.class);
        verify(dependentRepository).save(captor.capture());
        Dependent savedDependent = captor.getValue();

        // Assert — CCCD phải là giá trị đã mã hoá
        assertEquals(ENCRYPTED_CCCD, savedDependent.getCccdPassportEncrypted(),
                "CCCD trong DB phải là giá trị AES-256 đã mã hoá, không phải raw value");

        // Assert — CCCD KHÔNG phải plaintext
        assertNotEquals(VALID_CCCD, savedDependent.getCccdPassportEncrypted(),
                "CCCD không được lưu dạng plaintext '034095012345' vào DB (BR-SYS-01 — Nghị định 13/2023)");

        // Verify encryptionService được gọi đúng 1 lần
        verify(encryptionService, times(1)).encrypt(VALID_CCCD);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-UC16-010 | HIGH | Booking không tìm thấy → 404 (MOD2-003)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TC-UC16-010 — bookingId không tồn tại trong DB.
     *
     * Spec (TDD_UC16_SPEC §4 TC-UC16-010):
     *   Given bookingId = 9999 không tồn tại (Optional.empty())
     *   When registerDependent(9999, dto) được gọi
     *   Then ném BusinessException với errorCode "MOD2-003"
     *
     * 🔴 RED: DependentServiceImpl chưa handle Optional.empty() → FAIL
     */
    @Test
    @DisplayName("TC-UC16-010 | HIGH | bookingId không tồn tại → BusinessException [MOD2-003]")
    void TC_UC16_010_registerDependent_bookingNotFound_throwsMOD2003() {
        // Arrange
        when(bookingRepository.findById(BOOKING_ID_NOT_FOUND)).thenReturn(Optional.empty());

        DependentRegistrationDTO dto = buildDto(VALID_CCCD);

        // Act & Assert
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> dependentService.registerDependent(BOOKING_ID_NOT_FOUND, dto),
                "bookingId không tồn tại phải ném BusinessException"
        );

        assertTrue(
                ex.getMessage().contains("MOD2-003") || ex.getMessage().toLowerCase().contains("not found")
                        || ex.getMessage().toLowerCase().contains("booking"),
                "Message phải chứa 'MOD2-003' hoặc 'not found'. Thực tế: " + ex.getMessage()
        );
        assertEquals("MOD2-003", ex.getErrorCode(),
                "errorCode phải là MOD2-003 (Booking not found)");

        // Verify: save KHÔNG được gọi
        verify(dependentRepository, never()).save(any());
    }
}
