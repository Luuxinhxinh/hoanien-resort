package com.kawai.services;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.CheckinServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ================================================================
 * KAWAI RESORT — TDD Unit Test cho CheckinService (UC12 / SRS UC-13)
 * ================================================================
 *
 * Tham chiếu tài liệu:
 * - SRS §2.1.12 : UC-12 View Expected Arrivals Matrix
 * - SRS §2.1.13 : UC-13 Check-In & Allocate Physical Rooms ← nguồn gốc gap
 * analysis
 * - EDS §13 : Kịch bản kiểm thử (KAWAI-EDS-MOD2-UC12-001)
 * - TDD §4 : Test Case Specification (KAWAI-TDD-MOD2-UC12-001)
 *
 * ── Test Cases hiện có (đã GREEN) ────────────────────────────────
 * TC-M2-011 (UC12.1) Check-in thành công — phòng → OCCUPIED, tạo Folio
 * TC-M2-012a (UC12.1) Check-in thất bại — phòng DIRTY → exception
 * TC-M2-012b (UC12.1) Check-in thất bại — phòng MAINTENANCE → exception
 * TC-M2-013 (UC12.2) Cập nhật Credit Limit thành công
 *
 * 
 * ── Test Cases bổ sung (gap từ SRS UC-13) ────────────────────────
 * TC-M2-016 (UC12.1) BookingDetail không tồn tại → exception
 * TC-M2-017 (UC12.1) Room không tồn tại → exception
 * TC-M2-018 (UC12.1) Booking chưa CONFIRMED (Pending) → exception
 * TC-M2-019 (UC12.1) BookingDetail đã CHECKED_IN → exception (idempotency
 * guard)
 * TC-M2-020 (UC12.1) Phòng đang Occupied → không được check-in
 * TC-M2-023 (UC12.2) updateCreditLimit — giá trị âm → exception (validation)
 * TC-M2-024 (UC12.2) updateCreditLimit — bookingDetail không tồn tại →
 * exception
 *
 * 
 * Business Rules liên quan:
 * BR-FO-03: Ràng buộc tuổi check-in ≥18
 * BR-FO-04: Luân chuyển trạng thái phòng (Vacant_Clean → Occupied → Dirty)
 * BR-FO-06: Hạn mức chi tiêu phòng (Credit Limit)
 * BR-FO-07: Dependent Validation
 * BR-FO-08: Khai báo tạm trú (thu thập CCCD/Hộ chiếu)
 * BR-HK-03: Chặn check-in phòng đang bảo trì
 *
 * EDS Error Codes:
 * MOD2-001 : Validation failed (dữ liệu không hợp lệ)
 * MOD2-002 : Room not available (phòng Dirty/Maintenance/Occupied)
 * MOD2-003 : Booking/Entity not found
 *
 * TDD Phase: 🔴 RED → 🟢 GREEN
 * Các test TC-M2-016..026 được viết TRƯỚC khi implement production code.
 * Service chưa implement sẽ ném UnsupportedOperationException hoặc
 * NoSuchElementException.
 *
 * @see com.kawai.services.interfaces.CheckinService
 * @see com.kawai.services.impl.CheckinServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC12/UC13 — Check-in / Check-out / Đổi phòng (CheckinService)")
class CheckinServiceUC12Test {

        // ===== Mocks =====

        @Mock
        private RoomBookingDetailRepository roomBookingDetailRepository;

        @Mock
        private RoomRepository roomRepository;

        @Mock
        private RoomBookingRepository roomBookingRepository;

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private DependentRepository dependentRepository;

        @Mock
        private MembershipTierRepository membershipTierRepository;

        @Mock
        private com.kawai.repositories.MaintenanceRequestRepository maintenanceRequestRepo;

        @Mock
        private com.kawai.services.interfaces.EmailService emailService;

        @InjectMocks
        private CheckinServiceImpl checkinService;

        // ===== Test Data Fixtures =====

        private Room sampleRoom; // Vacant_Clean — sẵn sàng check-in
        private Room dirtyRoom; // Dirty
        private Room maintenanceRoom; // Maintenance
        private Room occupiedRoom; // Occupied — đang có khách
        private RoomCategory sampleCategory;
        private RoomBooking sampleRoomBooking;
        private RoomBookingDetail sampleBookingDetail;
        private RoomBookingDetail checkedInDetail; // detail đã CHECKED_IN rồi
        private Customer sampleCustomer;
        private Dependent sampleDependent;

        @BeforeEach
        void setUp() {
                // Room Category
                sampleCategory = new RoomCategory();
                sampleCategory.setId(1L);
                sampleCategory.setCategoryName("Deluxe");
                sampleCategory.setBasePrice(new BigDecimal("2000000"));

                // Room Vacant_Clean — sẵn sàng check-in
                sampleRoom = new Room();
                sampleRoom.setId(100L);
                sampleRoom.setRoomNumber("R101");
                sampleRoom.setCategory(sampleCategory);
                sampleRoom.setRoomStatus("Vacant_Clean");

                // Room DIRTY
                dirtyRoom = new Room();
                dirtyRoom.setId(101L);
                dirtyRoom.setRoomNumber("R102");
                dirtyRoom.setCategory(sampleCategory);
                dirtyRoom.setRoomStatus("Vacant_Dirty");

                // Room MAINTENANCE
                maintenanceRoom = new Room();
                maintenanceRoom.setId(102L);
                maintenanceRoom.setRoomNumber("R103");
                maintenanceRoom.setCategory(sampleCategory);
                maintenanceRoom.setRoomStatus("Maintenance");

                // Room OCCUPIED — đang có khách khác
                occupiedRoom = new Room();
                occupiedRoom.setId(103L);
                occupiedRoom.setRoomNumber("R104");
                occupiedRoom.setCategory(sampleCategory);
                occupiedRoom.setRoomStatus("Occupied");

                // Customer mẫu
                sampleCustomer = new Customer();
                sampleCustomer.setId(1L);
                sampleCustomer.setFullName("Nguyen Van A");
                sampleCustomer.setGender("MALE");
                sampleCustomer.setPhone("0987654321");
                sampleCustomer.setEmail("nguyenvana@email.com");
                sampleCustomer.setLoyaltyPoints(0);
                MembershipTier mockTier = new MembershipTier();
                mockTier.setTierName("Regular");
                mockTier.setCreditLimit(new BigDecimal("5000000.00"));
                sampleCustomer.setMembershipTier(mockTier);

                // RoomBooking CONFIRMED (điều kiện hợp lệ để check-in)
                sampleRoomBooking = new RoomBooking();
                sampleRoomBooking.setId(1000L);
                sampleRoomBooking.setCustomer(sampleCustomer);
                sampleRoomBooking.setBookingDate(LocalDate.of(2026, 6, 10));
                sampleRoomBooking.setTotalPrice(new BigDecimal("10000000"));
                sampleRoomBooking.setBookingStatus("CONFIRMED");
                sampleRoomBooking.setBookingSource("Direct_Web");
                sampleRoomBooking.setCheckInDate(LocalDate.of(2026, 6, 15));
                sampleRoomBooking.setCheckOutDate(LocalDate.of(2026, 6, 20));
                sampleRoomBooking.setDepositAmount(new BigDecimal("5000000"));
                sampleRoomBooking.setCreditLimit(new BigDecimal("5000000"));
                sampleRoomBooking.setPersonalPinHash("hashed_pin_123");

                // BookingDetail Pending — chưa gán phòng
                sampleBookingDetail = new RoomBookingDetail();
                sampleBookingDetail.setId(5000L);
                sampleBookingDetail.setRoomBooking(sampleRoomBooking);
                sampleBookingDetail.setCategory(sampleCategory);
                sampleBookingDetail.setRoom(null); // Chưa gán phòng
                sampleBookingDetail.setRoomCharge(BigDecimal.ZERO);
                sampleBookingDetail.setDetailStatus("Pending");
                sampleBookingDetail.setIsChargeToRoomAllowed(true);
                sampleBookingDetail.setSubCreditLimit(new BigDecimal("5000000"));
                sampleBookingDetail.setBillingRoutingStrategy("BILL_TO_LEADER");

                // BookingDetail đã CHECKED_IN (dùng cho idempotency test)
                checkedInDetail = new RoomBookingDetail();
                checkedInDetail.setId(5001L);
                checkedInDetail.setRoomBooking(sampleRoomBooking);
                checkedInDetail.setCategory(sampleCategory);
                checkedInDetail.setRoom(sampleRoom);
                checkedInDetail.setRoomCharge(new BigDecimal("2000000"));
                checkedInDetail.setDetailStatus("CHECKED_IN");
                checkedInDetail.setIsChargeToRoomAllowed(true);
                checkedInDetail.setSubCreditLimit(new BigDecimal("5000000"));
                checkedInDetail.setBillingRoutingStrategy("BILL_TO_LEADER");

                // Dependent mẫu
                sampleDependent = new Dependent();
                sampleDependent.setId(200L);
                sampleDependent.setCustomer(sampleCustomer);
                sampleDependent.setDependentName("Nguyen Thi B");
                sampleDependent.setBirthDate(LocalDate.of(2000, 3, 15));
                sampleDependent.setGender("FEMALE");
                sampleDependent.setCccdPassportEncrypted("encrypted_cccd_456");
        }

        // ================================================================
        // TC-M2-011: Check-in thành công — phòng chuyển OCCUPIED, tạo Folio
        // Ref: SRS UC-13 Normal Flow step 4, 7, 8
        // EDS TC-UNIT-UC12-001 | TDD TC-UC12-001
        // ================================================================
        @Nested
        @DisplayName("TC-M2-011: Check-in thành công — phòng chuyển OCCUPIED, tạo Folio")
        class TC_M2_011 {

                @Test
                @DisplayName("TC-M2-011: Check-in thành công — detail→CHECKED_IN, room→Occupied")
                void checkIn_Success_RoomBecomesOccupied_FolioCreated() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        Long roomId = 100L;

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));
                        when(roomRepository.findById(roomId))
                                        .thenReturn(Optional.of(sampleRoom));
                        when(roomBookingDetailRepository.save(any(RoomBookingDetail.class)))
                                        .thenAnswer(invocation -> {
                                                RoomBookingDetail saved = invocation.getArgument(0);
                                                saved.setDetailStatus("CHECKED_IN");
                                                return saved;
                                        });
                        when(roomRepository.save(any(Room.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        // ACT
                        RoomBookingDetail result = checkinService.checkIn(bookingDetailId, roomId,
                                        java.math.BigDecimal.ZERO);

                        // ASSERT
                        assertNotNull(result, "Kết quả check-in không được null");
                        assertEquals("CHECKED_IN", result.getDetailStatus(),
                                        "Trạng thái detail phải chuyển sang CHECKED_IN");
                        assertEquals(sampleRoom, result.getRoom(),
                                        "Phòng phải được gán cho booking detail");
                        assertEquals("Occupied", sampleRoom.getRoomStatus(),
                                        "Trạng thái phòng phải chuyển sang Occupied (BR-FO-04)");

                        // Verify interactions
                        verify(roomBookingDetailRepository).findById(bookingDetailId);
                        verify(roomRepository).findById(roomId);
                        verify(roomBookingDetailRepository, times(2)).save(any(RoomBookingDetail.class));
                        verify(roomRepository).save(any(Room.class));
                }
        }

        // ================================================================
        // TC-M2-012: Check-in thất bại — phòng DIRTY / MAINTENANCE → báo lỗi
        // Ref: SRS UC-13 Preconditions "available clean physical room"
        // EDS §6.2, §10 (MOD2-002) | TDD TC-UC12-002
        // BR-FO-04, BR-HK-03
        // ================================================================
        @Nested
        @DisplayName("TC-M2-012: Check-in thất bại — phòng DIRTY/MAINTENANCE → exception")
        class TC_M2_012 {

                @Test
                @DisplayName("TC-M2-012a: Check-in thất bại — phòng DIRTY → IllegalStateException")
                void checkIn_Fail_RoomDirty_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        Long roomId = 101L; // dirtyRoom

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));
                        when(roomRepository.findById(roomId))
                                        .thenReturn(Optional.of(dirtyRoom));

                        // ACT & ASSERT
                        IllegalStateException exception = assertThrows(
                                        IllegalStateException.class,
                                        () -> checkinService.checkIn(bookingDetailId, roomId,
                                                        java.math.BigDecimal.ZERO),
                                        "Phòng DIRTY phải ném IllegalStateException (BR-FO-04, MOD2-002)");
                        assertTrue(exception.getMessage().toLowerCase().contains("dirty"),
                                        "Message lỗi phải chứa thông tin trạng thái DIRTY");

                        // Verify: KHÔNG ghi DB vì check-in bị từ chối
                        verify(roomBookingDetailRepository, never()).save(any());
                        verify(roomRepository, never()).save(any());
                }

                @Test
                @DisplayName("TC-M2-012b: Check-in thất bại — phòng MAINTENANCE → IllegalStateException")
                void checkIn_Fail_RoomMaintenance_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        Long roomId = 102L; // maintenanceRoom

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));
                        when(roomRepository.findById(roomId))
                                        .thenReturn(Optional.of(maintenanceRoom));

                        // ACT & ASSERT
                        IllegalStateException exception = assertThrows(
                                        IllegalStateException.class,
                                        () -> checkinService.checkIn(bookingDetailId, roomId,
                                                        java.math.BigDecimal.ZERO),
                                        "Phòng MAINTENANCE phải ném IllegalStateException (BR-HK-03, MOD2-002)");
                        assertTrue(exception.getMessage().toLowerCase().contains("maintenance"),
                                        "Message lỗi phải chứa thông tin trạng thái MAINTENANCE");

                        verify(roomBookingDetailRepository, never()).save(any());
                        verify(roomRepository, never()).save(any());
                }
        }

        // ================================================================
        // TC-M2-013: Ủy quyền hạn mức — cập nhật Credit Limit thành công
        // Ref: SRS UC-14 step 5; EDS §8.1 updateCreditLimit() | TDD TC-UC12-003
        // BR-FO-06
        // ================================================================
        @Nested
        @DisplayName("TC-M2-013: Ủy quyền hạn mức — cập nhật Credit Limit thành công")
        class TC_M2_013 {

                @Test
                @DisplayName("TC-M2-013: updateCreditLimit thành công — roomBooking.creditLimit đổi sang giá trị mới")
                void updateCreditLimit_Success_ShouldUpdateCreditLimit() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        BigDecimal newCreditLimit = new BigDecimal("3000000"); // <= master limit (5,000,000)

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));
                        when(roomBookingDetailRepository.findByRoomBookingId(sampleRoomBooking.getId()))
                                        .thenReturn(java.util.Collections.singletonList(sampleBookingDetail));
                        when(roomBookingDetailRepository.save(any(RoomBookingDetail.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        // ACT
                        checkinService.updateCreditLimit(bookingDetailId, newCreditLimit);

                        // ASSERT
                        assertEquals(newCreditLimit, sampleBookingDetail.getSubCreditLimit(),
                                        "Credit Limit của phòng (subCreditLimit) phải được cập nhật thành giá trị mới (BR-FO-06)");

                        // Verify interactions
                        verify(roomBookingDetailRepository).findById(bookingDetailId);
                        verify(roomBookingDetailRepository).findByRoomBookingId(sampleRoomBooking.getId());
                        verify(roomBookingDetailRepository).save(any(RoomBookingDetail.class));
                }
        }

        // ================================================================
        // ── PHẦN BỔ SUNG — GAP TỪ SRS §2.1.13 ─────────────────────────
        // ================================================================

        // ================================================================
        // TC-M2-016: BookingDetail không tồn tại → exception
        // Ref: SRS UC-13 Preconditions "Guest has a valid confirmed reservation"
        // EDS §10 MOD2-003 | Gap: không có test cho trường hợp not found
        // ================================================================
        @Nested
        @DisplayName("TC-M2-016 [GAP]: checkIn — bookingDetail không tồn tại → exception")
        class TC_M2_016 {

                @Test
                @DisplayName("TC-M2-016: checkIn với bookingDetailId không tồn tại → exception (MOD2-003)")
                void checkIn_BookingDetailNotFound_ShouldThrowException() {
                        // ARRANGE
                        Long nonExistentDetailId = 9999L;
                        Long roomId = 100L;

                        when(roomBookingDetailRepository.findById(nonExistentDetailId))
                                        .thenReturn(Optional.empty());

                        // ACT & ASSERT
                        // Kỳ vọng: service ném IllegalArgumentException hoặc RuntimeException
                        // khi bookingDetailId không tồn tại (EDS MOD2-003)
                        assertThrows(
                                        RuntimeException.class,
                                        () -> checkinService.checkIn(nonExistentDetailId, roomId,
                                                        java.math.BigDecimal.ZERO),
                                        "BookingDetail không tồn tại phải ném RuntimeException (MOD2-003)");

                        // Verify: KHÔNG gọi roomRepository vì đã fail sớm
                        verify(roomRepository, never()).findById(any());
                        verify(roomBookingDetailRepository, never()).save(any());
                }
        }

        // ================================================================
        // TC-M2-017: Room không tồn tại → exception
        // Ref: SRS UC-13 step 4 "assigns an available clean physical room"
        // EDS §10 MOD2-003 | Gap: không có test cho room not found
        // ================================================================
        @Nested
        @DisplayName("TC-M2-017 [GAP]: checkIn — room không tồn tại → exception")
        class TC_M2_017 {

                @Test
                @DisplayName("TC-M2-017: checkIn với roomId không tồn tại → exception (MOD2-003)")
                void checkIn_RoomNotFound_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        Long nonExistentRoomId = 8888L;

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));
                        when(roomRepository.findById(nonExistentRoomId))
                                        .thenReturn(Optional.empty());

                        // ACT & ASSERT
                        assertThrows(
                                        RuntimeException.class,
                                        () -> checkinService.checkIn(bookingDetailId, nonExistentRoomId,
                                                        java.math.BigDecimal.ZERO),
                                        "Room không tồn tại phải ném RuntimeException (MOD2-003)");

                        verify(roomBookingDetailRepository, never()).save(any());
                        verify(roomRepository, never()).save(any());
                }
        }

        // ================================================================
        // TC-M2-018: Booking chưa CONFIRMED (Pending) → exception
        // Ref: SRS UC-13 Preconditions "Reservation status is eligible for check-in"
        // Gap: EDS/TDD không có test kiểm tra booking status
        // ================================================================
        @Nested
        @DisplayName("TC-M2-018 [GAP]: checkIn — booking status Pending → exception")
        class TC_M2_018 {

                @Test
                @DisplayName("TC-M2-018: checkIn với booking Pending → exception (chưa đủ điều kiện)")
                void checkIn_BookingNotConfirmed_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        Long roomId = 100L;

                        // Đặt booking về Pending — chưa hoàn thành đặt cọc
                        sampleRoomBooking.setBookingStatus("Pending");

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));

                        // ACT & ASSERT
                        IllegalStateException exception = assertThrows(
                                        IllegalStateException.class,
                                        () -> checkinService.checkIn(bookingDetailId, roomId,
                                                        java.math.BigDecimal.ZERO),
                                        "Booking chưa CONFIRMED không được phép check-in");
                        assertTrue(
                                        exception.getMessage().toLowerCase().contains("confirmed")
                                                        || exception.getMessage().toLowerCase().contains("pending"),
                                        "Message lỗi phải đề cập đến trạng thái booking không hợp lệ");

                        verify(roomBookingDetailRepository, never()).save(any());
                        verify(roomRepository, never()).save(any());
                }
        }

        // ================================================================
        // TC-M2-019: BookingDetail đã CHECKED_IN → idempotency guard
        // Ref: SRS UC-13 Postconditions "Room status is updated to Occupied"
        // Gap: không có guard chống double check-in
        // ================================================================
        @Nested
        @DisplayName("TC-M2-019 [GAP]: checkIn — detail đã CHECKED_IN → exception (idempotency guard)")
        class TC_M2_019 {

                @Test
                @DisplayName("TC-M2-019: checkIn với detail đã CHECKED_IN → exception (double check-in guard)")
                void checkIn_DetailAlreadyCheckedIn_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5001L; // checkedInDetail
                        Long roomId = 100L;

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(checkedInDetail));
                        // Không cần mock roomRepository — service nên fail trước

                        // ACT & ASSERT
                        IllegalStateException exception = assertThrows(
                                        IllegalStateException.class,
                                        () -> checkinService.checkIn(bookingDetailId, roomId,
                                                        java.math.BigDecimal.ZERO),
                                        "BookingDetail đã CHECKED_IN không được check-in lại (idempotency guard)");
                        assertTrue(
                                        exception.getMessage().toLowerCase().contains("checked_in")
                                                        || exception.getMessage().toLowerCase().contains("already"),
                                        "Message lỗi phải đề cập đến trạng thái đã check-in");

                        verify(roomBookingDetailRepository, never()).save(any());
                }
        }

        // ================================================================
        // TC-M2-020: Phòng đang Occupied → không được check-in (guard bổ sung)
        // Ref: SRS UC-13 Preconditions "available clean physical room"
        // EDS Invariant: DIRTY/MAINTENANCE không được phép check-in
        // Gap: Occupied cũng cần bị chặn nhưng chưa có test
        // ================================================================
        @Nested
        @DisplayName("TC-M2-020 [GAP]: checkIn — phòng Occupied → exception (MOD2-002)")
        class TC_M2_020 {

                @Test
                @DisplayName("TC-M2-020: checkIn vào phòng đang Occupied → IllegalStateException")
                void checkIn_RoomOccupied_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        Long roomId = 103L; // occupiedRoom

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));
                        when(roomRepository.findById(roomId))
                                        .thenReturn(Optional.of(occupiedRoom));

                        // ACT & ASSERT
                        IllegalStateException exception = assertThrows(
                                        IllegalStateException.class,
                                        () -> checkinService.checkIn(bookingDetailId, roomId,
                                                        java.math.BigDecimal.ZERO),
                                        "Phòng đang Occupied không được check-in (MOD2-002)");
                        assertTrue(
                                        exception.getMessage().toLowerCase().contains("occupied"),
                                        "Message lỗi phải đề cập trạng thái Occupied");

                        verify(roomBookingDetailRepository, never()).save(any());
                        verify(roomRepository, never()).save(any());
                }
        }

        // ================================================================
        // TC-M2-023: updateCreditLimit — giá trị âm → validation exception
        // Ref: EDS §4.2 Data Integrity; Gap: không có validation cho credit limit âm
        // BR-FO-06: Hạn mức chi tiêu phòng (Credit Limit)
        // ================================================================
        @Nested
        @DisplayName("TC-M2-023 [GAP]: updateCreditLimit — giá trị âm → exception (MOD2-001)")
        class TC_M2_023 {

                @Test
                @DisplayName("TC-M2-023: updateCreditLimit với giá trị âm → exception (MOD2-001)")
                void updateCreditLimit_NegativeValue_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        BigDecimal negativeCreditLimit = new BigDecimal("-1000000");

                        // ACT & ASSERT
                        IllegalArgumentException exception = assertThrows(
                                        IllegalArgumentException.class,
                                        () -> checkinService.updateCreditLimit(bookingDetailId, negativeCreditLimit),
                                        "Credit Limit âm phải ném IllegalArgumentException (MOD2-001)");
                        assertTrue(
                                        exception.getMessage().toLowerCase().contains("âm")
                                                        || exception.getMessage().toLowerCase().contains("negative")
                                                        || exception.getMessage().toLowerCase().contains("limit"),
                                        "Message lỗi phải giải thích giá trị không hợp lệ");

                        // Verify: không lưu gì vì bị reject ở validation
                        verify(roomBookingRepository, never()).save(any());
                }

                @Test
                @DisplayName("TC-M2-023b: updateCreditLimit với zero → được phép (edge case)")
                void updateCreditLimit_ZeroValue_ShouldSucceed() {
                        // ARRANGE — giá trị 0 có thể hợp lệ (reset hạn mức)
                        Long bookingDetailId = 5000L;
                        BigDecimal zeroCreditLimit = BigDecimal.ZERO;

                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));
                        when(roomBookingDetailRepository.findByRoomBookingId(sampleRoomBooking.getId()))
                                        .thenReturn(java.util.Collections.singletonList(sampleBookingDetail));
                        when(roomBookingDetailRepository.save(any(RoomBookingDetail.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        // ACT — zero credit limit KHÔNG nên ném exception
                        assertDoesNotThrow(
                                        () -> checkinService.updateCreditLimit(bookingDetailId, zeroCreditLimit),
                                        "Credit Limit = 0 là hợp lệ (vô hiệu hóa charge-to-room)");

                        verify(roomBookingDetailRepository, times(1)).save(any(RoomBookingDetail.class));
                }
        }

        // ================================================================
        // TC-M2-024: updateCreditLimit — bookingDetail không tồn tại → exception
        // Ref: EDS §10 MOD2-003 | Gap: chỉ test happy path updateCreditLimit
        // ================================================================
        @Nested
        @DisplayName("TC-M2-024 [GAP]: updateCreditLimit — bookingDetail không tồn tại → exception")
        class TC_M2_024 {

                @Test
                @DisplayName("TC-M2-024: updateCreditLimit với ID không tồn tại → exception (MOD2-003)")
                void updateCreditLimit_DetailNotFound_ShouldThrowException() {
                        // ARRANGE
                        Long nonExistentDetailId = 9999L;
                        BigDecimal newCreditLimit = new BigDecimal("3000000");

                        when(roomBookingDetailRepository.findById(nonExistentDetailId))
                                        .thenReturn(Optional.empty());

                        // ACT & ASSERT
                        assertThrows(
                                        RuntimeException.class,
                                        () -> checkinService.updateCreditLimit(nonExistentDetailId, newCreditLimit),
                                        "BookingDetail không tồn tại phải ném RuntimeException (MOD2-003)");

                        verify(roomBookingRepository, never()).save(any());
                }
        }

        // ================================================================
        // TC-M2-026: updateCreditLimit — Tổng hạn mức phòng vượt master customer limit
        // → [MOD2-UC14-016]
        // Ref: TDD UC12 | Gap: Phân tiền cho phòng phải <= tổng hạn mức tổng
        // ================================================================
        @Nested
        @DisplayName("TC-M2-026 [NEW]: updateCreditLimit — Tổng hạn mức phòng vượt master customer limit")
        class TC_M2_026 {

                @Test
                @DisplayName("TC-M2-026: updateCreditLimit với giá trị làm tổng hạn mức phòng vượt tổng hạn mức tài khoản tổng -> exception")
                void updateCreditLimit_ExceedsMasterLimit_ShouldThrowException() {
                        // ARRANGE
                        Long bookingDetailId = 5000L;
                        BigDecimal newCreditLimit = new BigDecimal("6000000"); // Master limit is 5000000

                        // Giả lập lấy ra detail
                        when(roomBookingDetailRepository.findById(bookingDetailId))
                                        .thenReturn(Optional.of(sampleBookingDetail));

                        // Giả lập lấy danh sách các detail của booking này (chỉ có detail hiện tại)
                        when(roomBookingDetailRepository
                                        .findByRoomBookingId(sampleBookingDetail.getRoomBooking().getId()))
                                        .thenReturn(java.util.Collections.singletonList(sampleBookingDetail));

                        // ACT & ASSERT
                        com.kawai.exceptions.BusinessException exception = assertThrows(
                                        com.kawai.exceptions.BusinessException.class,
                                        () -> checkinService.updateCreditLimit(bookingDetailId, newCreditLimit),
                                        "Phân bổ vượt hạn mức tổng phải ném BusinessException (MOD2-UC14-016)");
                        assertEquals("MOD2-UC14-016", exception.getErrorCode());

                        verify(roomBookingDetailRepository, never()).save(any());
                }
        }

}
