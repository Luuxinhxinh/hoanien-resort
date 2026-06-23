package com.kawai.receptionist;

import com.kawai.exceptions.BusinessException;
import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.Dependent;
import com.kawai.models.Room;
import com.kawai.models.RoomBooking;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.DependentRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.repositories.RoomBookingRepository;
import com.kawai.repositories.RoomRepository;

// ── UC-14 DTOs / Service (chưa tồn tại — RED phase) ──────────────────────────
import com.kawai.dto.walkin.WalkInCheckInRequest;
import com.kawai.dto.walkin.WalkInCheckInResponse;
import com.kawai.dto.walkin.DependentGuestDTO;
import com.kawai.services.interfaces.WalkInCheckInService;
import com.kawai.services.impl.WalkInCheckInServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC-14: Walk-in Guest Check-in
 * MODULE 2: Đặt phòng & Tiền sảnh vận hành
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Standard : ISO/IEC/IEEE 29119-3:2021
 * TDD Phase : 🔴 RED — Tất cả test PHẢI FAIL trước khi implement.
 * Document : KAWAI-TDD-MOD2-UC14-001 v1.2 (2026-06-21)
 * Test File : WalkInCheckInServiceUC14Test.java
 *
 * Ánh xạ test case (TDD_UC14_SPEC.md — §4):
 * ┌─────────────┬──────────────────────────────────────────────────────────────┬──────────┐
 * │ TC ID │ Kịch bản │ Severity │
 * ├─────────────┼──────────────────────────────────────────────────────────────┼──────────┤
 * │ TC-M2-021 │ Walk-in thành công: Booking + OCCUPIED + Account tạo mới │
 * CRITICAL │
 * │ TC-M2-023 │ E-01: CCCD sai format → từ chối │ HIGH │
 * │ │ E-01: dateOfBirth null → từ chối │ HIGH │
 * │ TC-M2-024 │ AF-01: Không có phòng trống → Walk-in bị chặn │ HIGH │
 * │ TC-M2-025 │ E-03: Transaction Rollback khi lỗi giữa chừng │ CRITICAL │
 * │ TC-M2-028 │ AF-02: Khách đã có profile → Reuse, không tạo duplicate │
 * MEDIUM │
 * │ TC-M2-029 │ AF-03: Thêm khách đi kèm → Dependent record │ MEDIUM │
 * │ TC-M2-030 │ E-02: Phòng DIRTY / MAINTENANCE → Walk-in bị chặn │ HIGH │
 * │ TC-M2-031 │ Auto-Create Customer Account cho khách Walk-in mới
 * (BR-08/09/10) │ HIGH │
 * │ TC-M2-033 │ numberOfGuests > roomCapacity → reject validation │ MEDIUM │
 * │ TC-M2-034 │ BR-07: ResidenceReporting fail → check-in OK (ADR-UC14-004) │
 * MEDIUM │
 * └─────────────┴──────────────────────────────────────────────────────────────┴──────────┘
 *
 * TC đã loại bỏ (v1.2):
 * ✖ TC-M2-023/2 (CCCD null): trẻ em có thể không có CCCD — không phải hard rule
 * ✖ TC-M2-023/3 (fullName rỗng): Bean Validation (@NotBlank) ở Controller —
 * không phải business logic UC-14
 * ✖ TC-M2-030/2 (MAINTENANCE riêng): gộp vào TC-M2-030 dưới
 * dạng @ParameterizedTest
 * ✖ TC-M2-032 (existing account reuse): nếu khách đã có Customer+Account,
 * receptionist tự biết không cần tạo mới — covered by TC-M2-028
 *
 * Business Rules kiểm thử:
 * BR-UC14-01 : CCCD bắt buộc, đúng format 12 chữ số (khi có)
 * BR-UC14-02 : Phòng phải Vacant_Clean, numberOfGuests <= capacity
 * BR-UC14-03 : Booking được tạo trong cùng 1 transaction
 * BR-UC14-05 : Booking status → CHECKED_IN sau check-in
 * BR-UC14-07 : ResidenceReporting là best-effort (ADR-UC14-004)
 * BR-UC14-08 : Tự động tạo Account cho khách mới
 * BR-UC14-09 : Default credentials được tạo
 * BR-UC14-10 : Account phải được link với Reservation
 * BR-ATOMIC-01: Toàn bộ walk-in là 1 @Transactional — ADR-UC14-003
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC-14 — Walk-in Guest Check-in | TDD 🔴 RED")
class WalkInCheckInServiceUC14Test {

        // ── System Under Test ─────────────────────────────────────────────────────
        @InjectMocks
        private WalkInCheckInServiceImpl walkInCheckInService;

        // ── Mocks ─────────────────────────────────────────────────────────────────
        @Mock
        private RoomRepository roomRepository;
        @Mock
        private RoomBookingRepository roomBookingRepository;
        @Mock
        private RoomBookingDetailRepository roomBookingDetailRepository;
        @Mock
        private CustomerRepository customerRepository;
        @Mock
        private AccountRepository accountRepository;
        @Mock
        private DependentRepository dependentRepository;
        @Mock
        private ApplicationEventPublisher eventPublisher;

        // ── Test Data Constants (SYNTHETIC) ──────────────────────────────────────
        private static final String CCCD_NEW_GUEST = "001234567890"; // TC-M2-021
        private static final String CCCD_EXISTING = "001200009999"; // TC-M2-028, customer_id=99
        private static final String CCCD_WITH_COMPANION = "001234567892"; // TC-M2-029
        private static final String CCCD_NEW_ACCOUNT = "001999888777"; // TC-M2-031

        private static final Long ROOM_301 = 301L; // TC-M2-021 — Vacant_Clean cap=2
        private static final Long ROOM_305 = 305L; // TC-M2-025 — Rollback
        private static final Long ROOM_308 = 308L; // TC-M2-028 — Reuse profile
        private static final Long ROOM_309 = 309L; // TC-M2-029 — Accompanied guest, cap=3
        private static final Long ROOM_310 = 310L; // TC-M2-030 — DIRTY
        private static final Long ROOM_311 = 311L; // TC-M2-030 — MAINTENANCE
        private static final Long ROOM_312 = 312L; // TC-M2-031 — Auto account
        private static final Long ROOM_314 = 314L; // TC-M2-033 — Capacity exceeded, cap=2
        private static final Long ROOM_315 = 315L; // TC-M2-034 — ResidenceReporting fail

        private static final Long CUSTOMER_ID_99 = 99L;

        // ── Helpers ───────────────────────────────────────────────────────────────

        private RoomCategory buildCategory(int capacity) {
                RoomCategory cat = new RoomCategory();
                cat.setId(1L);
                cat.setCategoryName("Standard");
                cat.setCapacity(capacity);
                cat.setBasePrice(new BigDecimal("1500000"));
                return cat;
        }

        private Room buildRoom(Long id, String number, String status, int capacity) {
                Room room = new Room();
                room.setId(id);
                room.setRoomNumber(number);
                room.setRoomStatus(status);
                room.setCategory(buildCategory(capacity));
                return room;
        }

        private Customer buildExistingCustomer(Long id, String fullName) {
                Customer c = new Customer();
                c.setId(id);
                c.setFullName(fullName);
                c.setEmail("existing" + id + "@kawai.test");
                c.setPhone("0901234567");
                c.setGender("Nam");
                c.setCccdPassportEncrypted("ENCRYPTED_" + id);
                return c;
        }

        private WalkInCheckInRequest buildRequest(String cccd, Long roomId, int numberOfGuests) {
                WalkInCheckInRequest req = new WalkInCheckInRequest();
                req.setFullName("Nguyen Van Test");
                req.setDateOfBirth(LocalDate.of(1990, 5, 15));
                req.setCccd(cccd);
                req.setPhone("0901234567");
                req.setEmail("nguyen.test@kawai.synthetic");
                req.setCheckInDate(LocalDate.now());
                req.setCheckOutDate(LocalDate.now().plusDays(2));
                req.setNumberOfGuests(numberOfGuests);
                req.setRoomId(roomId);
                return req;
        }

        @BeforeEach
        void setUp() {
                lenient().when(roomBookingRepository.save(any(RoomBooking.class))).thenAnswer(inv -> {
                        RoomBooking rb = inv.getArgument(0);
                        if (rb.getId() == null)
                                rb.setId(1001L);
                        return rb;
                });
                lenient().when(roomBookingDetailRepository.save(any(RoomBookingDetail.class))).thenAnswer(inv -> {
                        RoomBookingDetail d = inv.getArgument(0);
                        if (d.getId() == null)
                                d.setId(2001L);
                        return d;
                });
                lenient().when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
                        Customer c = inv.getArgument(0);
                        if (c.getId() == null)
                                c.setId(5001L);
                        return c;
                });
                lenient().when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
                        Account a = inv.getArgument(0);
                        if (a.getId() == null)
                                a.setId(6001L);
                        return a;
                });
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-021 | CRITICAL | Walk-in thành công: toàn bộ state machine
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-021 — Happy path: Walk-in đầy đủ 16 bước.
         *
         * Given Phòng R301 Vacant_Clean capacity=2, chưa có Customer với CCCD
         * 001234567890
         * When createWalkInBookingAndCheckIn() được gọi với dữ liệu hợp lệ
         * Then Response: bookingId not-null, roomNumber="R301", status="CHECKED_IN"
         * Room R301 → OCCUPIED
         * RoomBooking → CHECKED_IN, bookingSource="WALK_IN"
         * RoomBookingDetail → CHECKED_IN
         * Customer mới được tạo
         * Account mới được tạo (BR-08), passwordHash not-null (BR-09)
         * RoomCheckedInEvent được publish
         *
         * 🔴 RED: WalkInCheckInServiceImpl chưa tồn tại → COMPILE ERROR
         */
        @Test
        @DisplayName("TC-M2-021 | CRITICAL | Happy path: bookingId not-null, Room=OCCUPIED, Booking=CHECKED_IN")
        void TC_M2_021_walkIn_happyPath_allStateMachinesTransitioned() {
                // Arrange
                when(roomRepository.findByIdWithPessimisticLock(ROOM_301))
                                .thenReturn(Optional.of(buildRoom(ROOM_301, "R301", "Vacant_Clean", 2)));
                when(customerRepository.findByCccdPassportEncrypted(anyString()))
                                .thenReturn(Optional.empty());

                WalkInCheckInRequest request = buildRequest(CCCD_NEW_GUEST, ROOM_301, 1);

                // Act
                WalkInCheckInResponse response = walkInCheckInService.createWalkInBookingAndCheckIn(request);

                // Assert — Response
                assertNotNull(response, "Response không được null");
                assertNotNull(response.getBookingId(), "bookingId phải được sinh ra");
                assertEquals("R301", response.getRoomNumber(), "roomNumber phải là R301");
                assertEquals("CHECKED_IN", response.getBookingStatus(), "status phải là CHECKED_IN");
                assertTrue(response.isNewCustomer(), "isNewCustomer phải true cho khách mới");

                // Assert — [State Transition] Room → OCCUPIED
                ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
                verify(roomRepository).save(roomCaptor.capture());
                assertEquals("OCCUPIED", roomCaptor.getValue().getRoomStatus(),
                                "[State Transition] Room R301 phải chuyển sang OCCUPIED");

                // Assert — [State Transition] RoomBooking → CHECKED_IN
                ArgumentCaptor<RoomBooking> bookingCaptor = ArgumentCaptor.forClass(RoomBooking.class);
                verify(roomBookingRepository, atLeastOnce()).save(bookingCaptor.capture());
                boolean hasCheckedIn = bookingCaptor.getAllValues().stream()
                                .anyMatch(b -> "CHECKED_IN".equals(b.getBookingStatus()));
                assertTrue(hasCheckedIn,
                                "[State Transition] RoomBooking phải có trạng thái CHECKED_IN");
                boolean isWalkIn = bookingCaptor.getAllValues().stream()
                                .anyMatch(b -> "WALK_IN".equals(b.getBookingSource()));
                assertTrue(isWalkIn, "bookingSource phải là WALK_IN");

                // Assert — [State Transition] RoomBookingDetail → CHECKED_IN
                ArgumentCaptor<RoomBookingDetail> detailCaptor = ArgumentCaptor.forClass(RoomBookingDetail.class);
                verify(roomBookingDetailRepository).save(detailCaptor.capture());
                assertEquals("CHECKED_IN", detailCaptor.getValue().getDetailStatus(),
                                "[State Transition] RoomBookingDetail phải có detailStatus=CHECKED_IN");

                // Assert — Customer mới được tạo
                verify(customerRepository, times(1)).save(any(Customer.class));

                // Assert — [BR-08] Account mới được tạo
                ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
                verify(accountRepository, times(1)).save(accountCaptor.capture());
                // Assert — [BR-09] passwordHash không null
                assertNotNull(accountCaptor.getValue().getPasswordHash(),
                                "[BR-09] passwordHash không được null — default password phải được gán");

                // Assert — Event được publish
                verify(eventPublisher, atLeastOnce()).publishEvent(any());
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-023 | HIGH | E-01: Dữ liệu nhận dạng không hợp lệ → từ chối
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * Lưu ý về CCCD null:
         * Trẻ em có thể không có CCCD — hệ thống không bắt buộc CCCD null là lỗi.
         * Không test CCCD=null ở đây.
         *
         * Lưu ý về fullName rỗng:
         * Validation @NotBlank được xử lý ở Controller layer (Bean Validation).
         * Không phải business logic của UC-14 — không test ở Service layer.
         */
        @Nested
        @DisplayName("TC-M2-023 | HIGH | E-01: Dữ liệu nhận dạng không hợp lệ")
        class TC_M2_023_InvalidIdentification {

                /**
                 * TC-M2-023 sub-test 1: CCCD sai format (không phải 12 chữ số).
                 *
                 * Given cccd = "INVALID_12" (không phải 12 chữ số)
                 * Then BusinessException [MOD2-UC14-003], "Invalid identification document"
                 * Không có Customer / Booking nào được tạo
                 *
                 * 🔴 RED: validateIdentity() chưa implement → FAIL
                 */
                @Test
                @DisplayName("TC-M2-023/1 | CCCD sai format 'INVALID_12' → [MOD2-UC14-003]")
                void invalidCccdFormat_throwsMOD2UC14003() {
                        WalkInCheckInRequest req = buildRequest("INVALID_12", ROOM_301, 1);

                        BusinessException ex = assertThrows(BusinessException.class,
                                        () -> walkInCheckInService.createWalkInBookingAndCheckIn(req),
                                        "CCCD sai format phải ném BusinessException");

                        assertEquals("MOD2-UC14-003", ex.getErrorCode());
                        assertTrue(ex.getMessage().toLowerCase().contains("invalid identification"),
                                        "Message phải chứa 'invalid identification'. Thực tế: " + ex.getMessage());

                        verify(customerRepository, never()).save(any());
                        verify(roomBookingRepository, never()).save(any());
                }

                /**
                 * TC-M2-023 sub-test 2: dateOfBirth = null → từ chối.
                 *
                 * Given dateOfBirth = null
                 * Then BusinessException, message chứa "Date of birth is required"
                 * Không có Customer / Booking nào được tạo
                 *
                 * 🔴 RED: validation chưa implement → FAIL
                 */
                @Test
                @DisplayName("TC-M2-023/2 | dateOfBirth null → 'Date of birth is required'")
                void nullDateOfBirth_throwsValidationError() {
                        WalkInCheckInRequest req = buildRequest(CCCD_NEW_GUEST, ROOM_301, 1);
                        req.setDateOfBirth(null);

                        BusinessException ex = assertThrows(BusinessException.class,
                                        () -> walkInCheckInService.createWalkInBookingAndCheckIn(req),
                                        "dateOfBirth null phải ném BusinessException");

                        assertTrue(ex.getMessage().contains("Date of birth is required")
                                        || ex.getMessage().toLowerCase().contains("date of birth"),
                                        "Message phải chứa 'Date of birth is required'. Thực tế: " + ex.getMessage());

                        verify(customerRepository, never()).save(any());
                        verify(roomBookingRepository, never()).save(any());
                }
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-024 | HIGH | AF-01: Không có phòng trống → Walk-in bị chặn
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-024 — Phòng chỉ định không tồn tại hoặc không Vacant_Clean.
         *
         * Given roomId không tồn tại trong DB (Optional.empty())
         * Then BusinessException [MOD2-UC14-004], "No available rooms..."
         * Không có Booking nào được tạo
         *
         * 🔴 RED: WalkInCheckInServiceImpl chưa tồn tại → COMPILE ERROR
         */
        @Test
        @DisplayName("TC-M2-024 | HIGH | AF-01: Không có phòng trống → [MOD2-UC14-004]")
        void TC_M2_024_noAvailableRoom_throwsMOD2UC14004() {
                when(roomRepository.findByIdWithPessimisticLock(anyLong()))
                                .thenReturn(Optional.empty());

                WalkInCheckInRequest request = buildRequest(CCCD_NEW_GUEST, 9999L, 1);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> walkInCheckInService.createWalkInBookingAndCheckIn(request),
                                "Không có phòng trống phải ném BusinessException");

                assertEquals("MOD2-UC14-004", ex.getErrorCode());
                assertTrue(ex.getMessage().toLowerCase().contains("no available rooms")
                                || ex.getMessage().toLowerCase().contains("available"),
                                "Message phải chứa 'No available rooms'. Thực tế: " + ex.getMessage());

                verify(roomBookingRepository, never()).save(any());
                verify(customerRepository, never()).save(any());
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-025 | CRITICAL | E-03: Transaction Rollback khi lỗi giữa chừng
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-025 — @Transactional rollback khi exception xảy ra sau khi Booking đã
         * save().
         *
         * Given Phòng R305 Vacant_Clean
         * accountRepository.save() throw RuntimeException (simulate partial failure)
         * Then BusinessException [MOD2-UC14-005], "Walk-in check-in failed. Transaction
         * rolled back"
         * Event KHÔNG được publish (transaction chưa commit)
         *
         * 🔴 RED: @Transactional boundary chưa implement → FAIL
         *
         * SRS: E-03, ADR-UC14-003 (ACID Transaction Boundary)
         */
        @Test
        @DisplayName("TC-M2-025 | CRITICAL | E-03: RuntimeException giữa chừng → [MOD2-UC14-005], event không publish")
        void TC_M2_025_runtimeException_throwsMOD2UC14005_eventNotPublished() {
                when(roomRepository.findByIdWithPessimisticLock(ROOM_305))
                                .thenReturn(Optional.of(buildRoom(ROOM_305, "R305", "Vacant_Clean", 2)));
                when(customerRepository.findByCccdPassportEncrypted(anyString()))
                                .thenReturn(Optional.empty());
                when(accountRepository.save(any(Account.class)))
                                .thenThrow(new RuntimeException("Simulated account creation failure"));

                WalkInCheckInRequest request = buildRequest("001305000005", ROOM_305, 1);
                request.setFullName("TC-M2-025-Guest");

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> walkInCheckInService.createWalkInBookingAndCheckIn(request),
                                "RuntimeException giữa chừng phải ném BusinessException");

                assertEquals("MOD2-UC14-005", ex.getErrorCode(),
                                "errorCode phải là MOD2-UC14-005 (Transaction rolled back)");
                assertTrue(ex.getMessage().toLowerCase().contains("transaction rolled back")
                                || ex.getMessage().toLowerCase().contains("failed"),
                                "Message phải chứa 'transaction rolled back'. Thực tế: " + ex.getMessage());

                // Event KHÔNG được publish khi transaction chưa commit
                verify(eventPublisher, never()).publishEvent(any());
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-028 | MEDIUM | AF-02: Khách đã có profile → Reuse, không tạo duplicate
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-028 — Customer đã tồn tại → tái sử dụng, không tạo mới.
         *
         * Given Customer customer_id=99 đã tồn tại (CCCD "001200009999" đã lưu)
         * Phòng R308 Vacant_Clean
         * When Walk-in với CCCD "001200009999"
         * Then HTTP 201, check-in thành công
         * customerRepository.save() KHÔNG được gọi (không duplicate)
         * Booking liên kết customer_id=99
         * isNewCustomer = false
         *
         * 🔴 RED: findOrCreateCustomer() chưa implement → FAIL
         *
         * SRS: AF-02, BR-06 (no duplicate PII)
         */
        @Test
        @DisplayName("TC-M2-028 | MEDIUM | AF-02: Khách đã có profile → reuse customer_id=99, isNewCustomer=false")
        void TC_M2_028_existingCustomer_reuseProfile_noDuplicate() {
                when(roomRepository.findByIdWithPessimisticLock(ROOM_308))
                                .thenReturn(Optional.of(buildRoom(ROOM_308, "R308", "Vacant_Clean", 2)));

                Customer existing = buildExistingCustomer(CUSTOMER_ID_99, "Nguyen Van Existing");
                when(customerRepository.findByCccdPassportEncrypted(anyString()))
                                .thenReturn(Optional.of(existing));

                WalkInCheckInRequest request = buildRequest(CCCD_EXISTING, ROOM_308, 1);

                WalkInCheckInResponse response = walkInCheckInService.createWalkInBookingAndCheckIn(request);

                assertNotNull(response);
                assertEquals("CHECKED_IN", response.getBookingStatus());
                assertFalse(response.isNewCustomer(), "isNewCustomer phải false — khách đã có profile");
                assertEquals(CUSTOMER_ID_99, response.getCustomerId(), "customerId phải là 99");

                // Không tạo Customer mới (BR-06 — no duplicate PII)
                verify(customerRepository, never()).save(any(Customer.class));

                // Booking liên kết customer_id=99
                ArgumentCaptor<RoomBooking> cap = ArgumentCaptor.forClass(RoomBooking.class);
                verify(roomBookingRepository, atLeastOnce()).save(cap.capture());
                cap.getAllValues().forEach(b -> assertEquals(CUSTOMER_ID_99, b.getCustomer().getId(),
                                "Booking phải liên kết customer_id=99"));

                // Phòng R308 → OCCUPIED
                ArgumentCaptor<Room> roomCap = ArgumentCaptor.forClass(Room.class);
                verify(roomRepository).save(roomCap.capture());
                assertEquals("OCCUPIED", roomCap.getValue().getRoomStatus());
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-029 | MEDIUM | AF-03: Thêm khách đi kèm → Dependent record
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-029 — Walk-in với 1 khách đi kèm → Dependent record được tạo.
         *
         * Given Phòng R309 Vacant_Clean capacity=3
         * When Walk-in với accompaniedGuests=[{fullName:"Nguyen Thi B",
         * cccd:"001234567893"}]
         * Then HTTP 201 thành công
         * dependentRepository.save() được gọi đúng 1 lần
         * Dependent.dependentName = "Nguyen Thi B"
         * Dependent liên kết primary customer
         * response.accompaniedGuestCount = 1
         *
         * 🔴 RED: accompaniedGuests handling chưa implement → FAIL
         *
         * SRS: AF-03, BR-07 (temporary residence compliance)
         */
        @Test
        @DisplayName("TC-M2-029 | MEDIUM | AF-03: Walk-in với khách đi kèm → Dependent record liên kết primary customer")
        void TC_M2_029_accompaniedGuest_dependentRecordCreated() {
                when(roomRepository.findByIdWithPessimisticLock(ROOM_309))
                                .thenReturn(Optional.of(buildRoom(ROOM_309, "R309", "Vacant_Clean", 3)));
                when(customerRepository.findByCccdPassportEncrypted(anyString()))
                                .thenReturn(Optional.empty());

                DependentGuestDTO companion = new DependentGuestDTO();
                companion.setFullName("Nguyen Thi B");
                companion.setDateOfBirth(LocalDate.of(1995, 3, 20));
                companion.setCccd("001234567893");

                WalkInCheckInRequest request = buildRequest(CCCD_WITH_COMPANION, ROOM_309, 2);
                request.setAccompaniedGuests(List.of(companion));

                WalkInCheckInResponse response = walkInCheckInService.createWalkInBookingAndCheckIn(request);

                assertNotNull(response);
                assertEquals("CHECKED_IN", response.getBookingStatus());
                assertEquals(1, response.getAccompaniedGuestCount(),
                                "accompaniedGuestCount phải = 1");

                ArgumentCaptor<Dependent> depCap = ArgumentCaptor.forClass(Dependent.class);
                verify(dependentRepository, times(1)).save(depCap.capture());
                assertEquals("Nguyen Thi B", depCap.getValue().getDependentName(),
                                "dependentName phải là 'Nguyen Thi B'");
                assertNotNull(depCap.getValue().getCustomer(),
                                "Dependent phải liên kết với primary customer");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-030 | HIGH | E-02: Phòng DIRTY / MAINTENANCE → Walk-in bị chặn
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-030 — Phòng không Vacant_Clean → reject walk-in.
         *
         * Given Phòng R310 trạng thái DIRTY (case 1)
         * Phòng R311 trạng thái MAINTENANCE (case 2)
         * Then BusinessException [MOD2-UC14-006], "Selected room is not available for
         * check-in"
         * Không có Booking nào được tạo
         *
         * 🔴 RED: room status guard chưa implement → FAIL
         *
         * SRS: BR-02, E-02, ADR-UC14-002
         */
        @ParameterizedTest(name = "TC-M2-030 | Phòng status=''{0}'' → [MOD2-UC14-006]")
        @ValueSource(strings = { "DIRTY", "MAINTENANCE" })
        @DisplayName("TC-M2-030 | HIGH | E-02: Phòng DIRTY / MAINTENANCE → [MOD2-UC14-006]")
        void TC_M2_030_unavailableRoomStatus_throwsMOD2UC14006(String roomStatus) {
                // Chọn roomId theo status để tránh collision
                Long roomId = "DIRTY".equals(roomStatus) ? ROOM_310 : ROOM_311;
                String roomNumber = "DIRTY".equals(roomStatus) ? "R310" : "R311";

                when(roomRepository.findByIdWithPessimisticLock(roomId))
                                .thenReturn(Optional.of(buildRoom(roomId, roomNumber, roomStatus, 2)));

                WalkInCheckInRequest req = buildRequest(CCCD_NEW_GUEST, roomId, 1);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> walkInCheckInService.createWalkInBookingAndCheckIn(req),
                                "Phòng " + roomStatus + " phải ném BusinessException");

                assertEquals("MOD2-UC14-006", ex.getErrorCode(),
                                "errorCode phải là MOD2-UC14-006 cho status=" + roomStatus);
                assertTrue(ex.getMessage().toLowerCase().contains("not available")
                                || ex.getMessage().toLowerCase().contains("selected room"),
                                "Message phải chứa 'not available'. Thực tế: " + ex.getMessage());

                verify(roomBookingRepository, never()).save(any());
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-031 | HIGH | Auto-Create Customer Account cho khách Walk-in mới
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-031 — BR-08/09/10: Khách mới → Account tự động tạo và link Booking.
         *
         * Given Không có Customer nào với CCCD "001999888777"
         * Phòng R312 Vacant_Clean
         * When Walk-in hoàn tất
         * Then accountRepository.save() được gọi đúng 1 lần (BR-08)
         * Account.passwordHash không null và không rỗng (BR-09)
         * Booking với status CHECKED_IN được lưu và liên kết customer mới (BR-10)
         * response.isNewCustomer = true
         *
         * 🔴 RED: autoCreateAccount() chưa implement → FAIL
         *
         * SRS: BR-08 (auto account), BR-09 (default password), BR-10 (link reservation)
         */
        @Test
        @DisplayName("TC-M2-031 | HIGH | [BR-08/09/10] Khách mới → Account auto-created, passwordHash not-null, link Booking")
        void TC_M2_031_newGuest_accountAutoCreated_linkedToReservation() {
                when(roomRepository.findByIdWithPessimisticLock(ROOM_312))
                                .thenReturn(Optional.of(buildRoom(ROOM_312, "R312", "Vacant_Clean", 2)));
                when(customerRepository.findByCccdPassportEncrypted(anyString()))
                                .thenReturn(Optional.empty());

                WalkInCheckInRequest request = buildRequest(CCCD_NEW_ACCOUNT, ROOM_312, 1);
                request.setFullName("TC-M2-031-Guest");

                WalkInCheckInResponse response = walkInCheckInService.createWalkInBookingAndCheckIn(request);

                assertNotNull(response);
                assertTrue(response.isNewCustomer(), "[BR-08] isNewCustomer phải true");

                // [BR-08] Đúng 1 Account được tạo
                ArgumentCaptor<Account> accountCap = ArgumentCaptor.forClass(Account.class);
                verify(accountRepository, times(1)).save(accountCap.capture());

                // [BR-09] passwordHash không null, không rỗng
                assertNotNull(accountCap.getValue().getPasswordHash(),
                                "[BR-09] passwordHash không được null");
                assertFalse(accountCap.getValue().getPasswordHash().isBlank(),
                                "[BR-09] passwordHash không được rỗng");

                // [BR-10] Booking CHECKED_IN được lưu
                ArgumentCaptor<RoomBooking> bookingCap = ArgumentCaptor.forClass(RoomBooking.class);
                verify(roomBookingRepository, atLeastOnce()).save(bookingCap.capture());
                assertTrue(bookingCap.getAllValues().stream()
                                .anyMatch(b -> "CHECKED_IN".equals(b.getBookingStatus())),
                                "[BR-10] Booking CHECKED_IN phải được lưu");
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-033 | MEDIUM | numberOfGuests vượt Room Capacity → Reject
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-033 — numberOfGuests > room.capacity → 400 validation error.
         *
         * Given Phòng R314 Vacant_Clean capacity=2
         * When Walk-in với numberOfGuests=4 (vượt capacity=2)
         * Then BusinessException [MOD2-UC14-009], "Number of guests exceeds room
         * capacity"
         * Không có Booking nào được tạo
         *
         * 🔴 RED: capacity validation chưa implement → FAIL
         *
         * SRS: BR-02 (capacity validation), Normal Flow Step 5
         */
        @Test
        @DisplayName("TC-M2-033 | MEDIUM | numberOfGuests=4 > capacity=2 → [MOD2-UC14-009]")
        void TC_M2_033_numberOfGuestsExceedsCapacity_throwsMOD2UC14009() {
                when(roomRepository.findByIdWithPessimisticLock(ROOM_314))
                                .thenReturn(Optional.of(buildRoom(ROOM_314, "R314", "Vacant_Clean", 2)));

                WalkInCheckInRequest request = buildRequest(CCCD_NEW_GUEST, ROOM_314, 4); // 4 > capacity=2

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> walkInCheckInService.createWalkInBookingAndCheckIn(request),
                                "numberOfGuests > capacity phải ném BusinessException");

                assertEquals("MOD2-UC14-009", ex.getErrorCode());
                assertTrue(ex.getMessage().toLowerCase().contains("exceeds room capacity")
                                || ex.getMessage().toLowerCase().contains("capacity"),
                                "Message phải chứa 'exceeds room capacity'. Thực tế: " + ex.getMessage());

                verify(roomBookingRepository, never()).save(any());
                verify(customerRepository, never()).save(any());
        }

        // ══════════════════════════════════════════════════════════════════════════
        // TC-M2-034 | MEDIUM | ResidenceReporting fail → Check-in vẫn thành công
        // ══════════════════════════════════════════════════════════════════════════

        /**
         * TC-M2-034 — ADR-UC14-004 (Option B): ResidenceReporting fail → check-in không
         * bị ảnh hưởng.
         *
         * Given Phòng R315 Vacant_Clean
         * ResidenceReporting service ném exception (sau khi @Transactional commit)
         * When Walk-in được gọi
         * Then Response 201, status = CHECKED_IN (check-in thành công — Option B:
         * non-blocking)
         * Room R315 → OCCUPIED
         * Booking CHECKED_IN được lưu
         * Exception từ reporting KHÔNG propagate lên caller
         *
         * 🔴 RED: ResidenceReporting best-effort handling chưa implement → FAIL
         *
         * ADR-UC14-004: failure → log WARN + retry queue, không rollback booking.
         * SRS: BR-07, Normal Flow Step 15
         */
        @Test
        @DisplayName("TC-M2-034 | MEDIUM | ResidenceReporting fail → check-in vẫn 201 CHECKED_IN (ADR-UC14-004 Option B)")
        void TC_M2_034_residenceReportingFails_checkInStillSucceeds_optionB() {
                when(roomRepository.findByIdWithPessimisticLock(ROOM_315))
                                .thenReturn(Optional.of(buildRoom(ROOM_315, "R315", "Vacant_Clean", 2)));
                when(customerRepository.findByCccdPassportEncrypted(anyString()))
                                .thenReturn(Optional.empty());

                // Simulate: ResidenceReporting service ném exception (được gọi sau commit)
                // Service phải catch exception này và không rethrow
                doThrow(new RuntimeException("[TEST] ResidenceReportingException"))
                                .when(eventPublisher)
                                .publishEvent(argThat(e -> e != null && e.toString().contains("Residence")));

                WalkInCheckInRequest request = buildRequest("001315000015", ROOM_315, 1);

                // Act — PHẢI thành công dù reporting fail
                WalkInCheckInResponse response;
                try {
                        response = walkInCheckInService.createWalkInBookingAndCheckIn(request);
                } catch (Exception e) {
                        fail("[ADR-UC14-004 Option B] Walk-in KHÔNG được fail vì ResidenceReporting exception. "
                                        + "Actual: " + e.getClass().getSimpleName() + " — " + e.getMessage());
                        return;
                }

                // Assert — Check-in thành công
                assertNotNull(response, "[Option B] Response không được null");
                assertEquals("CHECKED_IN", response.getBookingStatus(),
                                "[Option B] status phải CHECKED_IN dù ResidenceReporting fail");

                // Assert — Room → OCCUPIED
                ArgumentCaptor<Room> roomCap = ArgumentCaptor.forClass(Room.class);
                verify(roomRepository).save(roomCap.capture());
                assertEquals("OCCUPIED", roomCap.getValue().getRoomStatus(),
                                "Phòng R315 phải chuyển sang OCCUPIED");

                // Assert — Booking CHECKED_IN được lưu
                ArgumentCaptor<RoomBooking> bookingCap = ArgumentCaptor.forClass(RoomBooking.class);
                verify(roomBookingRepository, atLeastOnce()).save(bookingCap.capture());
                assertTrue(bookingCap.getAllValues().stream()
                                .anyMatch(b -> "CHECKED_IN".equals(b.getBookingStatus())),
                                "Booking CHECKED_IN phải được lưu dù ResidenceReporting fail");
        }
}
