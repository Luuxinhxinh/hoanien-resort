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
 * KAWAI RESORT — TDD Unit Test cho CheckinService (UC12)
 * ================================================================
 *
 * Test Case tham chiếu:
 * - TC-M2-011 (UC12.1): Check-in thành công — phòng chuyển OCCUPIED, tạo Folio
 * - TC-M2-012 (UC12.1): Check-in thất bại — phòng đang DIRTY hoặc MAINTENANCE →
 * báo lỗi
 * - TC-M2-013 (UC12.2): Ủy quyền hạn mức — cập nhật Credit Limit thành công
 * - TC-M2-014 (UC12.3): Đổi phòng — chuyển Folio sang phòng mới, phòng cũ →
 * DIRTY
 * - TC-M2-015 (UC12.4): Nâng cấp Dependent thành Customer — tạo Account mới
 *
 * Business Rules liên quan:
 * - BR-FO-04: Luân chuyển trạng thái phòng (Vacant_Clean → Occupied → Dirty)
 * - BR-FO-06: Hạn mức chi tiêu phòng (Credit Limit)
 * - BR-FO-08: Khai báo tạm trú
 * - BR-HK-03: Chặn Check-in phòng đang bảo trì
 *
 * TDD Phase: 🔴 RED — Test được viết TRƯỚC khi implement production code.
 * Service hiện tại ném UnsupportedOperationException.
 *
 * @see com.kawai.services.interfaces.CheckinService
 * @see com.kawai.services.impl.CheckinServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC12 — Check-in / Check-out / Đổi phòng (CheckinService)")
class CheckinServiceUC12Test {

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

    @InjectMocks
    private CheckinServiceImpl checkinService;

    // ===== Test Data Fixtures =====

    private Room sampleRoom;
    private Room dirtyRoom;
    private Room maintenanceRoom;
    private RoomCategory sampleCategory;
    private RoomBooking sampleRoomBooking;
    private RoomBookingDetail sampleBookingDetail;
    private Customer sampleCustomer;
    private Dependent sampleDependent;

    @BeforeEach
    void setUp() {
        // Room Category
        sampleCategory = new RoomCategory();
        sampleCategory.setId(1L);
        sampleCategory.setCategoryName("Deluxe");
        sampleCategory.setBasePrice(new BigDecimal("2000000"));

        // Room mẫu — Vacant_Clean (sẵn sàng Check-in)
        sampleRoom = new Room();
        sampleRoom.setId(100L);
        sampleRoom.setRoomNumber("R101");
        sampleRoom.setCategory(sampleCategory);
        sampleRoom.setRoomStatus("Vacant_Clean");

        // Room DIRTY (chưa dọn)
        dirtyRoom = new Room();
        dirtyRoom.setId(101L);
        dirtyRoom.setRoomNumber("R102");
        dirtyRoom.setCategory(sampleCategory);
        dirtyRoom.setRoomStatus("Dirty");

        // Room MAINTENANCE (đang sửa chữa)
        maintenanceRoom = new Room();
        maintenanceRoom.setId(102L);
        maintenanceRoom.setRoomNumber("R103");
        maintenanceRoom.setCategory(sampleCategory);
        maintenanceRoom.setRoomStatus("Maintenance");

        // Customer mẫu
        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setFullName("Nguyen Van A");
        sampleCustomer.setGender("MALE");
        sampleCustomer.setPhone("0987654321");
        sampleCustomer.setEmail("nguyenvana@email.com");
        sampleCustomer.setLoyaltyPoints(0);
        sampleCustomer.setMembershipTier("Regular");

        // RoomBooking mẫu
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

        // RoomBookingDetail mẫu
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
    // ================================================================
    @Nested
    @DisplayName("TC-M2-011: Check-in thành công — phòng chuyển OCCUPIED, tạo Folio")
    class TC_M2_011 {

        @Test
        @DisplayName("TC-M2-011: Check-in thành công — phòng chuyển OCCUPIED, tạo Folio")
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
            RoomBookingDetail result = checkinService.checkIn(bookingDetailId, roomId);

            // ASSERT
            assertNotNull(result, "Kết quả check-in không được null");
            assertEquals("CHECKED_IN", result.getDetailStatus(),
                    "Trạng thái detail phải chuyển sang CHECKED_IN");
            assertEquals(sampleRoom, result.getRoom(),
                    "Phòng phải được gán cho booking detail");
            assertEquals("Occupied", sampleRoom.getRoomStatus(),
                    "Trạng thái phòng phải chuyển sang Occupied");

            // Verify interactions
            verify(roomBookingDetailRepository).findById(bookingDetailId);
            verify(roomRepository).findById(roomId);
            verify(roomBookingDetailRepository).save(any(RoomBookingDetail.class));
            verify(roomRepository).save(any(Room.class));
        }
    }

    // ================================================================
    // TC-M2-012: Check-in thất bại — phòng đang DIRTY hoặc MAINTENANCE → báo lỗi
    // ================================================================
    @Nested
    @DisplayName("TC-M2-012: Check-in thất bại — phòng DIRTY/MAINTENANCE → báo lỗi")
    class TC_M2_012 {

        @Test
        @DisplayName("TC-M2-012a: Check-in thất bại — phòng đang DIRTY → ném IllegalStateException")
        void checkIn_Fail_RoomDirty_ShouldThrowException() {
            // ARRANGE
            Long bookingDetailId = 5000L;
            Long roomId = 101L; // Room DIRTY

            when(roomBookingDetailRepository.findById(bookingDetailId))
                    .thenReturn(Optional.of(sampleBookingDetail));
            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(dirtyRoom));

            // ACT & ASSERT
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> checkinService.checkIn(bookingDetailId, roomId),
                    "Phòng DIRTY phải ném IllegalStateException (BR-FO-04, ROOM-001)");
            assertTrue(exception.getMessage().contains("DIRTY") || exception.getMessage().contains("dirty"),
                    "Thông báo lỗi phải chứa thông tin về trạng thái DIRTY");

            // Verify KHÔNG gọi save vì check-in bị từ chối
            verify(roomBookingDetailRepository, never()).save(any());
            verify(roomRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC-M2-012b: Check-in thất bại — phòng đang MAINTENANCE → ném IllegalStateException")
        void checkIn_Fail_RoomMaintenance_ShouldThrowException() {
            // ARRANGE
            Long bookingDetailId = 5000L;
            Long roomId = 102L; // Room MAINTENANCE

            when(roomBookingDetailRepository.findById(bookingDetailId))
                    .thenReturn(Optional.of(sampleBookingDetail));
            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(maintenanceRoom));

            // ACT & ASSERT
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> checkinService.checkIn(bookingDetailId, roomId),
                    "Phòng MAINTENANCE phải ném IllegalStateException (BR-HK-03, ROOM-001)");
            assertTrue(exception.getMessage().contains("MAINTENANCE") || exception.getMessage().contains("maintenance"),
                    "Thông báo lỗi phải chứa thông tin về trạng thái MAINTENANCE");

            // Verify KHÔNG gọi save
            verify(roomBookingDetailRepository, never()).save(any());
            verify(roomRepository, never()).save(any());
        }
    }

    // ================================================================
    // TC-M2-013: Ủy quyền hạn mức — cập nhật Credit Limit thành công
    // ================================================================
    @Nested
    @DisplayName("TC-M2-013: Ủy quyền hạn mức — cập nhật Credit Limit thành công")
    class TC_M2_013 {

        @Test
        @DisplayName("TC-M2-013: Cập nhật Credit Limit thành công — roomBooking.creditLimit thay đổi")
        void updateCreditLimit_Success_ShouldUpdateCreditLimit() {
            // ARRANGE
            Long bookingDetailId = 5000L;
            BigDecimal newCreditLimit = new BigDecimal("8000000");

            when(roomBookingDetailRepository.findById(bookingDetailId))
                    .thenReturn(Optional.of(sampleBookingDetail));
            when(roomBookingRepository.save(any(RoomBooking.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ACT
            checkinService.updateCreditLimit(bookingDetailId, newCreditLimit);

            // ASSERT
            assertEquals(newCreditLimit, sampleRoomBooking.getCreditLimit(),
                    "Credit Limit phải được cập nhật thành giá trị mới");

            // Verify interactions
            verify(roomBookingDetailRepository).findById(bookingDetailId);
            verify(roomBookingRepository).save(any(RoomBooking.class));
        }
    }

    // ================================================================
    // TC-M2-014: Đổi phòng — chuyển Folio sang phòng mới, phòng cũ → DIRTY
    // ================================================================
    @Nested
    @DisplayName("TC-M2-014: Đổi phòng — chuyển Folio sang phòng mới, phòng cũ → DIRTY")
    class TC_M2_014 {

        @Test
        @DisplayName("TC-M2-014: Đổi phòng thành công — room gán mới, phòng cũ → Dirty")
        void transferRoom_Success_RoomTransferred_OldRoomDirty() {
            // ARRANGE
            Long bookingDetailId = 5000L;
            Long newRoomId = 101L; // Phòng mới (dùng dirtyRoom mock nhưng sẽ set Vacant_Clean)

            Room newRoom = new Room();
            newRoom.setId(101L);
            newRoom.setRoomNumber("R102");
            newRoom.setCategory(sampleCategory);
            newRoom.setRoomStatus("Vacant_Clean"); // Phòng mới sẵn sàng

            // Giả sử booking detail đang ở phòng R101
            sampleBookingDetail.setRoom(sampleRoom);
            sampleBookingDetail.setDetailStatus("CHECKED_IN");

            when(roomBookingDetailRepository.findById(bookingDetailId))
                    .thenReturn(Optional.of(sampleBookingDetail));
            when(roomRepository.findById(newRoomId))
                    .thenReturn(Optional.of(newRoom));
            when(roomBookingDetailRepository.save(any(RoomBookingDetail.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(roomRepository.save(any(Room.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ACT
            RoomBookingDetail result = checkinService.transferRoom(bookingDetailId, newRoomId);

            // ASSERT
            assertNotNull(result, "Kết quả đổi phòng không được null");
            assertEquals(newRoom, result.getRoom(),
                    "Booking detail phải được gán sang phòng mới");
            assertEquals("Occupied", newRoom.getRoomStatus(),
                    "Phòng mới phải chuyển sang Occupied");
            assertEquals("Dirty", sampleRoom.getRoomStatus(),
                    "Phòng cũ phải chuyển sang Dirty (BR-FO-04)");

            // Verify interactions
            verify(roomBookingDetailRepository).findById(bookingDetailId);
            verify(roomRepository).findById(newRoomId);
            verify(roomBookingDetailRepository).save(any(RoomBookingDetail.class));
            verify(roomRepository, times(2)).save(any(Room.class)); // Lưu cả phòng cũ và mới
        }
    }

    // ================================================================
    // TC-M2-015: Nâng cấp Dependent thành Customer — tạo Account mới
    // ================================================================
    @Nested
    @DisplayName("TC-M2-015: Nâng cấp Dependent thành Customer — tạo Account mới")
    class TC_M2_015 {

        @Test
        @DisplayName("TC-M2-015: Nâng cấp Dependent → Customer mới với Account")
        void upgradeDependentToCustomer_Success_ShouldCreateNewCustomerWithAccount() {
            // ARRANGE
            Long dependentId = 200L;

            Role customerRole = new Role();
            customerRole.setId(2L);
            customerRole.setRoleName("CUSTOMER");

            when(dependentRepository.findById(dependentId))
                    .thenReturn(Optional.of(sampleDependent));
            when(roleRepository.findByRoleName("CUSTOMER"))
                    .thenReturn(Optional.of(customerRole));
            when(accountRepository.save(any(Account.class)))
                    .thenAnswer(invocation -> {
                        Account saved = invocation.getArgument(0);
                        saved.setId(50L);
                        return saved;
                    });
            when(customerRepository.save(any(Customer.class)))
                    .thenAnswer(invocation -> {
                        Customer saved = invocation.getArgument(0);
                        saved.setId(50L);
                        return saved;
                    });

            // ACT
            Customer result = checkinService.upgradeDependentToCustomer(dependentId);

            // ASSERT
            assertNotNull(result, "Kết quả nâng cấp không được null");
            assertEquals("Nguyen Thi B", result.getFullName(),
                    "Tên Customer phải khớp với tên Dependent");
            assertEquals("FEMALE", result.getGender(),
                    "Giới tính phải khớp");
            assertNotNull(result.getAccount(),
                    "Customer mới phải có Account được tạo");
            assertEquals("CUSTOMER", result.getAccount().getRole().getRoleName(),
                    "Role của Account phải là CUSTOMER");

            // Verify interactions
            verify(dependentRepository).findById(dependentId);
            verify(roleRepository).findByRoleName("CUSTOMER");
            verify(accountRepository).save(any(Account.class));
            verify(customerRepository).save(any(Customer.class));
        }
    }
}