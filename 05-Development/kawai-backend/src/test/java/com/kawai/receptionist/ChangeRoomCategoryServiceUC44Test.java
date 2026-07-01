package com.kawai.receptionist;

import com.kawai.exceptions.BusinessException;
import com.kawai.models.*;
import com.kawai.repositories.*;

// ============================================================
// UC-44 DTO / Service chưa implement → RED phase
// Các class dưới đây chưa tồn tại trong production code.
// Mục tiêu: compile FAIL → xác nhận RED → implement → GREEN.
// ============================================================
import com.kawai.services.interfaces.ChangeRoomCategoryService;
import com.kawai.dto.roomchange.ChangeRoomCategoryRequest;
import com.kawai.dto.roomchange.ChangeRoomCategoryResponse;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════
 *  JUnit 5 Test ─ UC-44: Change Room Category (Đổi hạng phòng)
 *  MODULE 2: Đặt phòng & Tiền sảnh vận hành
 * ═══════════════════════════════════════════════════════════════════════
 *
 *  Standard  : ISO/IEC/IEEE 29119-3:2021
 *  TDD Phase : 🔴 RED ─ Tất cả test phải FAIL trước khi implement.
 *  Document  : KAWAI-TDD-UC44-001 v1.0 (2026-06-29)
 *  Test File : ChangeRoomCategoryServiceUC44Test.java
 *
 *  Ánh xạ test case (TDD_UC44_Change_Room_Category.md § 4):
 *  ┌─────────────────┬────────────────────────────────────────────────────┬──────────┐
 *  │ TC ID           │ Kịch bản                                           │ Severity │
 *  ├─────────────────┼────────────────────────────────────────────────────┼──────────┤
 *  │ TC-UC44-001     │ Upgrade → phụ phí đúng, phòng cũ DIRTY            │ CRITICAL │
 *  │ TC-UC44-002     │ Downgrade → không hoàn tiền (BR-FIN-09)           │ HIGH     │
 *  │ TC-UC44-003     │ Không có phòng trống ở hạng yêu cầu (AF1)         │ HIGH     │
 *  │ TC-UC44-004     │ Khách từ chối rate adjustment (AF2)                │ MEDIUM   │
 *  │ TC-UC44-005     │ Same-rate category change — không tạo FolioItem   │ MEDIUM   │
 *  │ TC-UC44-006     │ Race Condition — phòng bị lấy mất (EX1)           │ CRITICAL │
 *  │ TC-UC44-007     │ Booking không IN-HOUSE → chặn (EX3, BR-FO-10)    │ HIGH     │
 *  │ TC-UC44-008     │ Lỗi DB → Transaction Rollback toàn bộ (EX2)      │ HIGH     │
 *  │ TC-UC44-009     │ Pricing config lỗi → báo lỗi rõ ràng (EX4)       │ MEDIUM   │
 *  │ TC-UC44-010     │ Audit Log ghi đầy đủ sau mỗi ca đổi phòng         │ HIGH     │
 *  └─────────────────┴────────────────────────────────────────────────────┴──────────┘
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC-44: Change Room Category — TDD 🔴 RED Phase")
class ChangeRoomCategoryServiceUC44Test {

    // ─── Mocks ────────────────────────────────────────────────────────────────
    @Mock private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock private RoomRepository              roomRepository;
    @Mock private FolioItemRepository         folioItemRepository;
    @Mock private AuditLogRepository          auditLogRepository;
    @Mock private RoomCategoryRepository      roomCategoryRepository;

    // ─── Subject Under Test ───────────────────────────────────────────────────
    @InjectMocks
    private com.kawai.services.impl.ChangeRoomCategoryServiceImpl changeRoomCategoryService;

    // ─── Shared Test Fixtures ─────────────────────────────────────────────────
    private RoomBookingDetail   activeDetail;
    private Room                oldRoom;
    private Room                newRoom;
    private RoomCategory        deluxeCategory;
    private RoomCategory        suiteCategory;
    private RoomBooking         roomBooking;
    private Account             receptionistAccount;

    /**
     * Constant IDs used across tests.
     */
    private static final Long BOOKING_DETAIL_ID   = 5001L;
    private static final Long OLD_ROOM_ID         = 201L;
    private static final Long NEW_ROOM_ID         = 301L;
    private static final Long RECEPTIONIST_ID     = 999L;

    @BeforeEach
    void setUp() {
        // ── Receptionist account ──
        receptionistAccount = new Account();
        receptionistAccount.setId(RECEPTIONIST_ID);

        // ── Room Categories ──
        deluxeCategory = new RoomCategory();
        deluxeCategory.setId(10L);
        deluxeCategory.setCategoryName("Deluxe");
        deluxeCategory.setBasePrice(new BigDecimal("2000000"));

        suiteCategory = new RoomCategory();
        suiteCategory.setId(20L);
        suiteCategory.setCategoryName("Suite");
        suiteCategory.setBasePrice(new BigDecimal("3500000"));

        // ── Old room (R201 — Deluxe, Occupied) ──
        oldRoom = new Room();
        oldRoom.setId(OLD_ROOM_ID);
        oldRoom.setRoomNumber("R201");
        oldRoom.setCategory(deluxeCategory);
        oldRoom.setRoomStatus("Occupied");

        // ── New room (R301 — Suite, Vacant_Clean) ──
        newRoom = new Room();
        newRoom.setId(NEW_ROOM_ID);
        newRoom.setRoomNumber("R301");
        newRoom.setCategory(suiteCategory);
        newRoom.setRoomStatus("Vacant_Clean");

        // ── RoomBooking (Checked_In, checkout 3 nights from now) ──
        roomBooking = new RoomBooking();
        roomBooking.setId(2001L);
        roomBooking.setBookingStatus("Checked_In");
        roomBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        roomBooking.setCheckInDate(LocalDate.now());

        // ── RoomBookingDetail linking booking ↔ old room ──
        activeDetail = new RoomBookingDetail();
        activeDetail.setId(BOOKING_DETAIL_ID);
        activeDetail.setRoomBooking(roomBooking);
        activeDetail.setRoom(oldRoom);
        activeDetail.setCategory(deluxeCategory);
        activeDetail.setDetailStatus("Checked_In");
        activeDetail.setRoomCharge(new BigDecimal("2000000"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-001 — Upgrade có phụ phí (CRITICAL)
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-001 | Upgrade — phụ phí đúng, phòng cũ → DIRTY")
    class TC_UC44_001_UpgradeWithSurcharge {

        /**
         * TC-UC44-001-A: Phụ phí = (newRate - oldRate) × remainingNights = 4,500,000 VND
         */
        @Test
        @DisplayName("001-A | FolioItem amount = (3,500,000 - 2,000,000) × 3 = 4,500,000 VND")
        void upgrade_shouldCreateFolioItemWithCorrectAmount() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            ChangeRoomCategoryResponse response = changeRoomCategoryService.changeCategory(request);

            // Assert — FolioItem phụ phí
            ArgumentCaptor<FolioItem> folioCaptor = ArgumentCaptor.forClass(FolioItem.class);
            verify(folioItemRepository, times(1)).save(folioCaptor.capture());
            FolioItem createdFolio = folioCaptor.getValue();

            assertNotNull(createdFolio, "FolioItem phải được tạo khi upgrade");
            assertEquals(new BigDecimal("4500000"), createdFolio.getAmount(),
                    "Phụ phí upgrade = (3,500,000 - 2,000,000) × 3 = 4,500,000 VND");
        }

        /**
         * TC-UC44-001-B: Phòng cũ phải chuyển → VACANT_DIRTY
         */
        @Test
        @DisplayName("001-B | Phòng cũ R201 chuyển trạng thái → VACANT_DIRTY")
        void upgrade_oldRoomShouldBeVacantDirty() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert — room status
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository, atLeastOnce()).save(roomCaptor.capture());

            List<Room> savedRooms = roomCaptor.getAllValues();
            boolean oldRoomDirty = savedRooms.stream()
                    .filter(r -> r.getId().equals(OLD_ROOM_ID))
                    .anyMatch(r -> "Vacant_Dirty".equalsIgnoreCase(r.getRoomStatus()));
            assertTrue(oldRoomDirty, "Phòng cũ R201 phải chuyển → Vacant_Dirty sau khi đổi phòng");
        }

        /**
         * TC-UC44-001-C: Phòng mới phải chuyển → OCCUPIED
         */
        @Test
        @DisplayName("001-C | Phòng mới R301 chuyển trạng thái → OCCUPIED")
        void upgrade_newRoomShouldBeOccupied() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert — new room → Occupied
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository, atLeastOnce()).save(roomCaptor.capture());

            boolean newRoomOccupied = roomCaptor.getAllValues().stream()
                    .filter(r -> r.getId().equals(NEW_ROOM_ID))
                    .anyMatch(r -> "Occupied".equalsIgnoreCase(r.getRoomStatus()));
            assertTrue(newRoomOccupied, "Phòng mới R301 phải chuyển → Occupied sau khi đổi phòng");
        }

        /**
         * TC-UC44-001-D: RoomBookingDetail phải được cập nhật room reference
         */
        @Test
        @DisplayName("001-D | RoomBookingDetail.room được cập nhật thành R301")
        void upgrade_roomBookingDetailShouldReferenceNewRoom() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert — RoomBookingDetail saved with new room
            ArgumentCaptor<RoomBookingDetail> detailCaptor = ArgumentCaptor.forClass(RoomBookingDetail.class);
            verify(roomBookingDetailRepository, atLeastOnce()).save(detailCaptor.capture());

            RoomBookingDetail saved = detailCaptor.getValue();
            assertEquals(NEW_ROOM_ID, saved.getRoom().getId(),
                    "RoomBookingDetail.room_id phải được cập nhật thành R301 (id=301)");
        }

        /**
         * TC-UC44-001-E: AuditLog phải được ghi sau upgrade thành công
         */
        @Test
        @DisplayName("001-E | AuditLog ghi với action = ROOM_CATEGORY_CHANGED sau upgrade")
        void upgrade_shouldCreateAuditLog() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert — audit log
            ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository, times(1)).save(auditCaptor.capture());
            AuditLog log = auditCaptor.getValue();

            assertEquals("ROOM_CATEGORY_CHANGED", log.getAction(),
                    "AuditLog.action phải là ROOM_CATEGORY_CHANGED");
            assertNotNull(log.getTimestamp(), "AuditLog.timestamp không được null");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-002 — Downgrade không hoàn tiền (HIGH, BR-FIN-09)
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-002 | Downgrade — không hoàn tiền (BR-FIN-09)")
    class TC_UC44_002_DowngradeNoRefund {

        private RoomBookingDetail suiteDetail;
        private Room              suiteOldRoom;
        private Room              deluxeNewRoom;

        @BeforeEach
        void setUpDowngrade() {
            // Suite room (old) — 3,500,000
            RoomCategory suite = new RoomCategory();
            suite.setId(20L);
            suite.setCategoryName("Suite");
            suite.setBasePrice(new BigDecimal("3500000"));

            suiteOldRoom = new Room();
            suiteOldRoom.setId(401L);
            suiteOldRoom.setRoomNumber("R401");
            suiteOldRoom.setCategory(suite);
            suiteOldRoom.setRoomStatus("Occupied");

            // Deluxe room (new) — 2,000,000
            deluxeNewRoom = new Room();
            deluxeNewRoom.setId(202L);
            deluxeNewRoom.setRoomNumber("R202");
            deluxeNewRoom.setCategory(deluxeCategory);
            deluxeNewRoom.setRoomStatus("Vacant_Clean");

            RoomBooking suiteBooking = new RoomBooking();
            suiteBooking.setId(2002L);
            suiteBooking.setBookingStatus("Checked_In");
            suiteBooking.setCheckOutDate(LocalDate.now().plusDays(2));
            suiteBooking.setCheckInDate(LocalDate.now());

            suiteDetail = new RoomBookingDetail();
            suiteDetail.setId(5002L);
            suiteDetail.setRoomBooking(suiteBooking);
            suiteDetail.setRoom(suiteOldRoom);
            suiteDetail.setCategory(suite);
            suiteDetail.setDetailStatus("Checked_In");
            suiteDetail.setRoomCharge(new BigDecimal("3500000"));
        }

        @Test
        @DisplayName("002-A | Downgrade — KHÔNG có FolioItem hoàn tiền nào được tạo")
        void downgrade_shouldNOT_createFolioItem() {
            // Arrange
            when(roomBookingDetailRepository.findById(5002L))
                    .thenReturn(Optional.of(suiteDetail));
            when(roomRepository.findByIdWithPessimisticLock(202L))
                    .thenReturn(Optional.of(deluxeNewRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(5002L)
                    .selectedRoomId(202L)
                    .targetCategoryName("Deluxe")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            ChangeRoomCategoryResponse response = changeRoomCategoryService.changeCategory(request);

            // Assert — NO folio item for downgrade
            verify(folioItemRepository, never()).save(any(FolioItem.class));
            assertNull(response.getFolioItemId(),
                    "Downgrade KHÔNG được tạo FolioItem hoàn tiền (BR-FIN-09)");
        }

        @Test
        @DisplayName("002-B | Downgrade — phòng cũ R401 → VACANT_DIRTY, phòng mới R202 → OCCUPIED")
        void downgrade_shouldUpdateRoomStatuses() {
            // Arrange
            when(roomBookingDetailRepository.findById(5002L))
                    .thenReturn(Optional.of(suiteDetail));
            when(roomRepository.findByIdWithPessimisticLock(202L))
                    .thenReturn(Optional.of(deluxeNewRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(5002L)
                    .selectedRoomId(202L)
                    .targetCategoryName("Deluxe")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert room statuses
            ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository, atLeastOnce()).save(captor.capture());

            List<Room> saved = captor.getAllValues();
            assertTrue(saved.stream()
                    .filter(r -> r.getId().equals(401L))
                    .anyMatch(r -> "Vacant_Dirty".equalsIgnoreCase(r.getRoomStatus())),
                    "Phòng cũ R401 phải chuyển → Vacant_Dirty");
            assertTrue(saved.stream()
                    .filter(r -> r.getId().equals(202L))
                    .anyMatch(r -> "Occupied".equalsIgnoreCase(r.getRoomStatus())),
                    "Phòng mới R202 phải chuyển → Occupied");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-003 — Không có phòng trống (AF1, BR-FO-11) — HIGH
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-003 | Không có phòng Vacant_Clean ở hạng yêu cầu (AF1)")
    class TC_UC44_003_NoAvailableRoom {

        @Test
        @DisplayName("003-A | Ném BusinessException với message 'No available rooms in the selected category.'")
        void noAvailableRoom_shouldThrowBusinessException() {
            // Arrange — booking is Checked_In but target room not found / not Vacant_Clean
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.empty()); // No room available

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> changeRoomCategoryService.changeCategory(request));
            assertTrue(ex.getMessage().contains("No available rooms in the selected category."),
                    "Message lỗi phải chứa 'No available rooms in the selected category.'");
        }

        @Test
        @DisplayName("003-B | Booking & Folio KHÔNG thay đổi khi không có phòng trống")
        void noAvailableRoom_shouldNOT_modifyBookingOrFolio() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.empty());

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            assertThrows(BusinessException.class, () -> changeRoomCategoryService.changeCategory(request));

            // Assert — NOTHING saved
            verify(roomBookingDetailRepository, never()).save(any());
            verify(folioItemRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-004 — Khách từ chối / Hủy yêu cầu (AF2) — MEDIUM
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-004 | Khách từ chối — cancelPendingChange() không tạo FolioItem")
    class TC_UC44_004_GuestDeclines {

        @Test
        @DisplayName("004-A | cancelPendingChange() — booking giữ nguyên phòng cũ")
        void cancelPendingChange_shouldKeepOriginalRoom() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));

            // Act — guest declines: call cancel endpoint
            changeRoomCategoryService.cancelPendingChange(BOOKING_DETAIL_ID, RECEPTIONIST_ID);

            // Assert — booking unchanged, no folio
            verify(folioItemRepository, never()).save(any(FolioItem.class));
            // RoomBookingDetail must NOT change room reference
            verify(roomBookingDetailRepository, never()).save(
                    argThat(d -> !d.getRoom().getId().equals(OLD_ROOM_ID)));
        }

        @Test
        @DisplayName("004-B | cancelPendingChange() — booking status vẫn Checked_In")
        void cancelPendingChange_bookingStatusRemainsInHouse() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));

            // Act
            changeRoomCategoryService.cancelPendingChange(BOOKING_DETAIL_ID, RECEPTIONIST_ID);

            // Assert
            assertEquals("Checked_In", activeDetail.getRoomBooking().getBookingStatus(),
                    "Booking status phải vẫn là Checked_In sau khi hủy yêu cầu đổi phòng");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-005 — Same-rate category change (AF3) — MEDIUM
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-005 | Same-rate category — không tạo FolioItem phụ phí (AF3)")
    class TC_UC44_005_SameRateChange {

        @Test
        @DisplayName("005-A | Hai hạng cùng giá 2,000,000 → KHÔNG tạo FolioItem")
        void sameRate_shouldNOT_createFolioItem() {
            // Arrange — new room same price as old (both 2,000,000)
            RoomCategory gardenCategory = new RoomCategory();
            gardenCategory.setId(11L);
            gardenCategory.setCategoryName("Deluxe Garden View");
            gardenCategory.setBasePrice(new BigDecimal("2000000")); // same price

            Room gardenRoom = new Room();
            gardenRoom.setId(203L);
            gardenRoom.setRoomNumber("R203");
            gardenRoom.setCategory(gardenCategory);
            gardenRoom.setRoomStatus("Vacant_Clean");

            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail)); // old rate = 2,000,000
            when(roomRepository.findByIdWithPessimisticLock(203L))
                    .thenReturn(Optional.of(gardenRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(203L)
                    .targetCategoryName("Deluxe Garden View")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            ChangeRoomCategoryResponse response = changeRoomCategoryService.changeCategory(request);

            // Assert — no folio item
            verify(folioItemRepository, never()).save(any(FolioItem.class));
            assertNull(response.getFolioItemId(), "Same-rate change KHÔNG được tạo FolioItem phụ phí (AF3)");
        }

        @Test
        @DisplayName("005-B | Same-rate — phòng cũ → VACANT_DIRTY, phòng mới → OCCUPIED")
        void sameRate_shouldUpdateRoomStatuses() {
            // Arrange
            RoomCategory gardenCategory = new RoomCategory();
            gardenCategory.setId(11L);
            gardenCategory.setCategoryName("Deluxe Garden View");
            gardenCategory.setBasePrice(new BigDecimal("2000000"));

            Room gardenRoom = new Room();
            gardenRoom.setId(203L);
            gardenRoom.setRoomNumber("R203");
            gardenRoom.setCategory(gardenCategory);
            gardenRoom.setRoomStatus("Vacant_Clean");

            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(203L))
                    .thenReturn(Optional.of(gardenRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(203L)
                    .targetCategoryName("Deluxe Garden View")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert
            ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository, atLeastOnce()).save(captor.capture());
            List<Room> saved = captor.getAllValues();

            assertTrue(saved.stream()
                    .filter(r -> r.getId().equals(OLD_ROOM_ID))
                    .anyMatch(r -> "Vacant_Dirty".equalsIgnoreCase(r.getRoomStatus())),
                    "Phòng cũ phải → Vacant_Dirty dù same-rate");
            assertTrue(saved.stream()
                    .filter(r -> r.getId().equals(203L))
                    .anyMatch(r -> "Occupied".equalsIgnoreCase(r.getRoomStatus())),
                    "Phòng mới R203 phải → Occupied sau same-rate change");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-006 — Race Condition (EX1, CWE-362) — CRITICAL
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-006 | Race Condition — phòng bị lấy mất (EX1, CWE-362)")
    class TC_UC44_006_RaceCondition {

        @Test
        @DisplayName("006 | Chỉ 1 trong 2 thread đồng thời thành công — không có double-assignment")
        void raceCondition_onlyOneThreadSucceeds() throws InterruptedException {
            // Arrange — R302 Suite (chỉ 1 phòng)
            RoomCategory suite = new RoomCategory();
            suite.setId(20L);
            suite.setCategoryName("Suite");
            suite.setBasePrice(new BigDecimal("3500000"));

            Room sharedRoom = new Room();
            sharedRoom.setId(302L);
            sharedRoom.setRoomNumber("R302");
            sharedRoom.setCategory(suite);
            sharedRoom.setRoomStatus("Vacant_Clean");

            // Booking for guest A
            RoomBooking bookingA = new RoomBooking();
            bookingA.setId(3001L);
            bookingA.setBookingStatus("Checked_In");
            bookingA.setCheckOutDate(LocalDate.now().plusDays(2));
            bookingA.setCheckInDate(LocalDate.now());

            RoomBookingDetail detailA = new RoomBookingDetail();
            detailA.setId(6001L);
            detailA.setRoomBooking(bookingA);
            detailA.setRoom(oldRoom);
            detailA.setCategory(deluxeCategory);
            detailA.setDetailStatus("Checked_In");
            detailA.setRoomCharge(new BigDecimal("2000000"));

            // Booking for guest B
            RoomBooking bookingB = new RoomBooking();
            bookingB.setId(3002L);
            bookingB.setBookingStatus("Checked_In");
            bookingB.setCheckOutDate(LocalDate.now().plusDays(2));
            bookingB.setCheckInDate(LocalDate.now());

            RoomBookingDetail detailB = new RoomBookingDetail();
            detailB.setId(6002L);
            detailB.setRoomBooking(bookingB);
            detailB.setRoom(oldRoom);
            detailB.setCategory(deluxeCategory);
            detailB.setDetailStatus("Checked_In");
            detailB.setRoomCharge(new BigDecimal("2000000"));

            // Simulate pessimistic lock: first caller gets room, second gets empty
            AtomicInteger callCount = new AtomicInteger(0);
            when(roomRepository.findByIdWithPessimisticLock(302L)).thenAnswer(inv -> {
                int call = callCount.incrementAndGet();
                if (call == 1) {
                    return Optional.of(sharedRoom);
                } else {
                    return Optional.empty(); // second thread → room already taken
                }
            });
            when(roomBookingDetailRepository.findById(6001L)).thenReturn(Optional.of(detailA));
            when(roomBookingDetailRepository.findById(6002L)).thenReturn(Optional.of(detailB));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount   = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2);

            Runnable taskA = () -> {
                try {
                    ChangeRoomCategoryRequest req = ChangeRoomCategoryRequest.builder()
                            .bookingDetailId(6001L).selectedRoomId(302L)
                            .targetCategoryName("Suite").receptionistAccountId(RECEPTIONIST_ID).build();
                    changeRoomCategoryService.changeCategory(req);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            };

            Runnable taskB = () -> {
                try {
                    ChangeRoomCategoryRequest req = ChangeRoomCategoryRequest.builder()
                            .bookingDetailId(6002L).selectedRoomId(302L)
                            .targetCategoryName("Suite").receptionistAccountId(RECEPTIONIST_ID).build();
                    changeRoomCategoryService.changeCategory(req);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            };

            // Act — launch both threads simultaneously
            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(taskA);
            executor.submit(taskB);
            latch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            // Assert — exactly 1 success, 1 fail (no double-assignment)
            assertEquals(1, successCount.get(), "Chỉ đúng 1 lễ tân thành công (không double-assignment)");
            assertEquals(1, failCount.get(),    "Lễ tân còn lại phải nhận lỗi room not available");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-007 — Booking không IN-HOUSE (EX3, BR-FO-10) — HIGH
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-007 | Booking không IN-HOUSE → từ chối (EX3, BR-FO-10)")
    class TC_UC44_007_NotInHouse {

        @Test
        @DisplayName("007-A | Booking CONFIRMED → ném BusinessException với message đúng")
        void notInHouse_CONFIRMED_shouldReject() {
            // Arrange — booking status = CONFIRMED (not Checked_In)
            RoomBooking confirmedBooking = new RoomBooking();
            confirmedBooking.setId(2004L);
            confirmedBooking.setBookingStatus("CONFIRMED");
            confirmedBooking.setCheckOutDate(LocalDate.now().plusDays(5));
            confirmedBooking.setCheckInDate(LocalDate.now().plusDays(1));

            RoomBookingDetail confirmedDetail = new RoomBookingDetail();
            confirmedDetail.setId(9999L);
            confirmedDetail.setRoomBooking(confirmedBooking);
            confirmedDetail.setRoom(oldRoom);
            confirmedDetail.setCategory(deluxeCategory);
            confirmedDetail.setDetailStatus("Pending");
            confirmedDetail.setRoomCharge(new BigDecimal("2000000"));

            when(roomBookingDetailRepository.findById(9999L))
                    .thenReturn(Optional.of(confirmedDetail));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(9999L)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> changeRoomCategoryService.changeCategory(request));
            assertTrue(ex.getMessage().contains("Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng."),
                    "Message phải chứa 'Chỉ khách đang lưu trú mới được phép thay đổi hạng phòng.'");
        }

        @Test
        @DisplayName("007-B | Booking CHECKED_OUT → ném BusinessException")
        void notInHouse_CHECKED_OUT_shouldReject() {
            // Arrange
            RoomBooking checkedOutBooking = new RoomBooking();
            checkedOutBooking.setId(2005L);
            checkedOutBooking.setBookingStatus("CHECKED_OUT");
            checkedOutBooking.setCheckOutDate(LocalDate.now().minusDays(1));
            checkedOutBooking.setCheckInDate(LocalDate.now().minusDays(3));

            RoomBookingDetail checkedOutDetail = new RoomBookingDetail();
            checkedOutDetail.setId(9998L);
            checkedOutDetail.setRoomBooking(checkedOutBooking);
            checkedOutDetail.setRoom(oldRoom);
            checkedOutDetail.setCategory(deluxeCategory);
            checkedOutDetail.setDetailStatus("Checked_Out");
            checkedOutDetail.setRoomCharge(new BigDecimal("2000000"));

            when(roomBookingDetailRepository.findById(9998L))
                    .thenReturn(Optional.of(checkedOutDetail));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(9998L)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act & Assert
            assertThrows(BusinessException.class,
                    () -> changeRoomCategoryService.changeCategory(request),
                    "Booking CHECKED_OUT cũng phải bị từ chối");
        }

        @Test
        @DisplayName("007-C | Khi bị từ chối, DB không bị thay đổi gì cả")
        void notInHouse_shouldNOT_modifyDatabase() {
            // Arrange
            RoomBooking pendingBooking = new RoomBooking();
            pendingBooking.setId(2006L);
            pendingBooking.setBookingStatus("Pending");
            pendingBooking.setCheckOutDate(LocalDate.now().plusDays(5));
            pendingBooking.setCheckInDate(LocalDate.now().plusDays(2));

            RoomBookingDetail pendingDetail = new RoomBookingDetail();
            pendingDetail.setId(9997L);
            pendingDetail.setRoomBooking(pendingBooking);
            pendingDetail.setRoom(oldRoom);
            pendingDetail.setCategory(deluxeCategory);
            pendingDetail.setDetailStatus("Pending");
            pendingDetail.setRoomCharge(new BigDecimal("2000000"));

            when(roomBookingDetailRepository.findById(9997L))
                    .thenReturn(Optional.of(pendingDetail));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(9997L)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            assertThrows(BusinessException.class,
                    () -> changeRoomCategoryService.changeCategory(request));

            // Assert — nothing saved
            verify(roomBookingDetailRepository, never()).save(any());
            verify(roomRepository, never()).save(any());
            verify(folioItemRepository, never()).save(any());
            verify(auditLogRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-008 — Lỗi DB → Transaction Rollback (EX2) — HIGH
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-008 | Lỗi DB → Transaction Rollback toàn bộ (EX2)")
    class TC_UC44_008_DbErrorRollback {

        @Test
        @DisplayName("008-A | RoomBookingDetailRepository.save() ném RuntimeException → rollback, ném BusinessException")
        void dbError_shouldRollbackAndThrow() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            // Simulate DB failure on saving detail
            when(roomBookingDetailRepository.save(any()))
                    .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB error"));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act & Assert — should surface a BusinessException wrapping the DB error
            assertThrows(RuntimeException.class,
                    () -> changeRoomCategoryService.changeCategory(request),
                    "Khi DB lỗi phải ném RuntimeException để trigger @Transactional rollback");
        }

        @Test
        @DisplayName("008-B | Sau DB lỗi, FolioItem KHÔNG được tạo (rollback toàn bộ)")
        void dbError_shouldNOT_createFolioItem() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomBookingDetailRepository.save(any()))
                    .thenThrow(new RuntimeException("Simulated DB failure"));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            assertThrows(RuntimeException.class,
                    () -> changeRoomCategoryService.changeCategory(request));

            // Assert — FolioItem KHÔNG được save do rollback
            verify(folioItemRepository, never()).save(any(FolioItem.class));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-009 — Pricing config lỗi (EX4) — MEDIUM
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-009 | DynamicPricingService lỗi → báo lỗi rõ ràng (EX4)")
    class TC_UC44_009_PricingError {

        @Test
        @DisplayName("009 | Phòng không có giá (basePrice null) → ném BusinessException với message phù hợp")
        void pricingError_shouldThrowBusinessException() {
            // Arrange — new room has no base price (simulate pricing config error)
            RoomCategory noPriceCategory = new RoomCategory();
            noPriceCategory.setId(99L);
            noPriceCategory.setCategoryName("SpecialCategory");
            noPriceCategory.setBasePrice(null); // No price configured

            Room noPriceRoom = new Room();
            noPriceRoom.setId(500L);
            noPriceRoom.setRoomNumber("R500");
            noPriceRoom.setCategory(noPriceCategory);
            noPriceRoom.setRoomStatus("Vacant_Clean");

            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(500L))
                    .thenReturn(Optional.of(noPriceRoom));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(500L)
                    .targetCategoryName("SpecialCategory")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> changeRoomCategoryService.changeCategory(request));
            assertTrue(ex.getMessage().contains("Không thể tính chênh lệch giá phòng"),
                    "Message phải chứa 'Không thể tính chênh lệch giá phòng'");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TC-UC44-010 — Audit Log đầy đủ (BR-SYS-04) — HIGH
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("TC-UC44-010 | Audit Log ghi đầy đủ mọi ca đổi phòng (BR-SYS-04)")
    class TC_UC44_010_AuditLog {

        @Test
        @DisplayName("010-A | AuditLog chứa đủ: action, recordId, oldValue, newValue, timestamp, ipAddress")
        void auditLog_shouldContainAllRequiredFields() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository, times(1)).save(captor.capture());
            AuditLog log = captor.getValue();

            assertAll("AuditLog phải chứa đủ các trường theo BR-SYS-04",
                    () -> assertEquals("ROOM_CATEGORY_CHANGED", log.getAction(),
                            "action phải là ROOM_CATEGORY_CHANGED"),
                    () -> assertNotNull(log.getRecordId(),
                            "recordId (booking_id hoặc detail_id) không được null"),
                    () -> assertNotNull(log.getOldValue(),
                            "oldValue (old room info) không được null"),
                    () -> assertNotNull(log.getNewValue(),
                            "newValue (new room info) không được null"),
                    () -> assertNotNull(log.getTimestamp(),
                            "timestamp không được null"),
                    () -> assertNotNull(log.getIpAddress(),
                            "ipAddress không được null (BR-SYS-04)")
            );
        }

        @Test
        @DisplayName("010-B | Audit Log ghi cho mỗi ca đổi phòng (không bỏ sót)")
        void auditLog_savedExactlyOncePerSuccessfulChange() {
            // Arrange
            when(roomBookingDetailRepository.findById(BOOKING_DETAIL_ID))
                    .thenReturn(Optional.of(activeDetail));
            when(roomRepository.findByIdWithPessimisticLock(NEW_ROOM_ID))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(folioItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ChangeRoomCategoryRequest request = ChangeRoomCategoryRequest.builder()
                    .bookingDetailId(BOOKING_DETAIL_ID)
                    .selectedRoomId(NEW_ROOM_ID)
                    .targetCategoryName("Suite")
                    .receptionistAccountId(RECEPTIONIST_ID)
                    .build();

            // Act
            changeRoomCategoryService.changeCategory(request);

            // Assert — exactly 1 audit log per change operation
            verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        }
    }
}
