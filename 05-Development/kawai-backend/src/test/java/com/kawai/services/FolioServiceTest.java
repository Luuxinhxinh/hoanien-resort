package com.kawai.services;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.FolioServiceImpl;
import com.kawai.services.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Folio Service Unit Tests — đã refactor từ FolioServiceTest.java + FolioServiceM5Test.java
 * Xóa toàn bộ fake test (assertNotNull(service)), giữ lại và bổ sung logic thực.
 * Target: ~20 test có assertion có ý nghĩa.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FolioService — Unit Tests (Refactored)")
public class FolioServiceTest {

    @Mock private FolioItemRepository folioItemRepository;
    @Mock private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private ConsolidatedInvoiceRepository consolidatedInvoiceRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmailService emailService;
    @Mock private com.kawai.services.interfaces.WorkflowEngineService workflowEngineService;

    @InjectMocks
    private FolioServiceImpl folioService;

    private RoomBookingDetail mockBookingDetail;
    private RoomBooking mockRoomBooking;
    private Customer mockCustomer;
    private Room mockRoom;
    private FolioItem mockItem;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(1L);
        mockCustomer.setEmail("test@test.com");

        mockRoomBooking = new RoomBooking();
        mockRoomBooking.setId(1L);
        mockRoomBooking.setCustomer(mockCustomer);

        mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setRoomNumber("101");
        mockRoom.setRoomStatus("Occupied");

        mockBookingDetail = new RoomBookingDetail();
        mockBookingDetail.setId(1L);
        mockBookingDetail.setRoomBooking(mockRoomBooking);
        mockBookingDetail.setRoom(mockRoom);
        mockBookingDetail.setDetailStatus("CHECKED_IN");
        mockBookingDetail.setRoomCharge(new BigDecimal("1000.00"));

        mockItem = new FolioItem();
        mockItem.setId(100L);
        mockItem.setRoomBookingDetail(mockBookingDetail);
        mockItem.setAmount(new BigDecimal("500.00"));
        mockItem.setSourceDepartment("F&B");
        mockItem.setDescription("Minibar charge");
    }

    // =========================================================================
    // UC_REC.1 — Theo dõi dư nợ Folio (Folio Balance)
    // =========================================================================
    @Nested
    @DisplayName("UC_REC.1 — Folio Balance")
    class FolioBalance {

        @Test
        @DisplayName("[TC-REC1-01] Lấy dư nợ hợp lệ — có FolioItem 500đ → trả về 500đ")
        void getFolioBalance_WithItems_ReturnsCorrectBalance() {
            when(folioItemRepository.findAll()).thenReturn(List.of(mockItem));
            BigDecimal balance = folioService.getFolioBalance(1L);
            assertEquals(0, new BigDecimal("500.00").compareTo(balance));
        }

        @Test
        @DisplayName("[TC-REC1-02] Phòng không có dư nợ → trả về 0")
        void getFolioBalance_NoItems_ReturnsZero() {
            when(folioItemRepository.findAll()).thenReturn(new ArrayList<>());
            BigDecimal balance = folioService.getFolioBalance(1L);
            assertEquals(0, BigDecimal.ZERO.compareTo(balance));
        }
    }

    // =========================================================================
    // UC_REC.2 — Ký nợ (Post-to-Room / addFolioItem)  [TC-M5-016]
    // =========================================================================
    @Nested
    @DisplayName("UC_REC.2 — Post-to-Room (TC-M5-016)")
    class PostToRoom {

        @Test
        @DisplayName("[TC-M5-016-01] Ký nợ thành công phòng đang CHECKED_IN — gọi save() đúng 1 lần")
        void addFolioItem_ValidCheckedIn_SavesSuccessfully() {
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.save(any(FolioItem.class))).thenAnswer(i -> i.getArgument(0));

            folioService.addFolioItem(1L, "F&B", new BigDecimal("250.00"), "Restaurant post");

            verify(folioItemRepository, times(1)).save(any(FolioItem.class));
        }

        @Test
        @DisplayName("[TC-M5-016-05] Ký nợ thất bại do bookingDetailId không tồn tại — ném IllegalArgumentException")
        void addFolioItem_InvalidBookingDetail_ThrowsException() {
            when(roomBookingDetailRepository.findById(999L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () ->
                folioService.addFolioItem(999L, "F&B", new BigDecimal("150.00"), "Test")
            );
            verify(folioItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("[TC-M5-016-02] Ký nợ thất bại — phòng ở trạng thái CHECKED_OUT → ném IllegalStateException, KHÔNG gọi save()")
        void addFolioItem_CheckedOutRoom_ThrowsAndNoSave() {
            mockBookingDetail.setDetailStatus("CHECKED_OUT");
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));

            // Service phải ném IllegalStateException (FOLIO-002) khi phòng đã CHECKED_OUT
            assertThrows(IllegalStateException.class, () ->
                folioService.addFolioItem(1L, "F&B", new BigDecimal("100.00"), "Late charge")
            );
            verify(folioItemRepository, never()).save(any());
        }
    }

    // =========================================================================
    // UC_REC.5 & UC_REC.7 — Check-out & Settlement  [TC-M5-022]
    // =========================================================================
    @Nested
    @DisplayName("UC_REC.7 — Check-out & Settle (TC-M5-022)")
    class CheckoutSettle {

        @Test
        @DisplayName("[TC-M5-022-01] Checkout khi còn dư nợ → ném IllegalStateException chứa FOLIO-001")
        void checkOutAndSettle_WithOutstandingBalance_ThrowsFolio001() {
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.findAll()).thenReturn(List.of(mockItem));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                folioService.checkOutAndSettle(1L, "CASH")
            );
            assertTrue(ex.getMessage().contains("FOLIO-001"),
                "Exception message phải chứa mã lỗi FOLIO-001 nhưng nhận được: " + ex.getMessage());
        }

        @Test
        @DisplayName("[TC-M5-022-02] Checkout khi dư nợ = 0 → CHECKED_OUT + Room trở về Vacant_Dirty")
        void checkOutAndSettle_ZeroBalance_SetsCheckedOutAndVacantDirty() {
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.findAll()).thenReturn(new ArrayList<>());
            when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));
            when(roomBookingDetailRepository.save(any(RoomBookingDetail.class))).thenAnswer(i -> i.getArgument(0));
            when(consolidatedInvoiceRepository.save(any(ConsolidatedInvoice.class))).thenAnswer(i -> i.getArgument(0));
            doAnswer(invocation -> {
                mockRoom.setRoomStatus("Vacant_Dirty");
                return null;
            }).when(workflowEngineService).triggerEvent(eq("ROOM_CHECKOUT"), anyMap());

            folioService.checkOutAndSettle(1L, "CASH");

            assertEquals("CHECKED_OUT", mockBookingDetail.getDetailStatus(),
                "Trạng thái booking detail phải là CHECKED_OUT");
            assertEquals("Vacant_Dirty", mockRoom.getRoomStatus(),
                "Trạng thái phòng phải là Vacant_Dirty sau khi checkout");
        }

        @Test
        @DisplayName("[TC-M5-022-08] Response error khi FOLIO-001 phải chứa đúng message code")
        void checkOutAndSettle_ErrorMessageContainsFolioCode() {
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.findAll()).thenReturn(List.of(mockItem));

            Exception ex = assertThrows(IllegalStateException.class, () ->
                folioService.checkOutAndSettle(1L, "CASH")
            );
            assertNotNull(ex.getMessage(), "Message không được null");
            assertTrue(ex.getMessage().contains("FOLIO-001"),
                "Phải chứa mã lỗi FOLIO-001 để FE có thể hiển thị đúng nội dung lỗi");
        }

        @Test
        @DisplayName("[TC-M5-022-10] Khi checkout thất bại — auditLogRepository.save() KHÔNG được gọi")
        void checkOutAndSettle_Failure_NoAuditLog() {
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.findAll()).thenReturn(List.of(mockItem));

            assertThrows(IllegalStateException.class, () ->
                folioService.checkOutAndSettle(1L, "CASH")
            );
            verify(auditLogRepository, never()).save(any());
        }
    }

    // =========================================================================
    // TC-M5-021 — Night Audit (tự động ghi charge phòng)
    // =========================================================================
    @Nested
    @DisplayName("TC-M5-021 — Night Audit")
    class NightAudit {

        @Test
        @DisplayName("[TC-M5-021-01] Night Audit thành công — gọi folioItemRepo.save() và auditLogRepo.save()")
        void performNightAudit_ValidData_SavesFolioAndAuditLog() {
            Employee staff = new Employee();
            staff.setId(10L);
            staff.setFullName("Nguyen Van A");

            when(roomBookingDetailRepository.findAll()).thenReturn(List.of(mockBookingDetail));
            when(employeeRepository.findById(10L)).thenReturn(Optional.of(staff));
            when(folioItemRepository.save(any(FolioItem.class))).thenAnswer(i -> i.getArgument(0));
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));

            folioService.performNightAudit(10L);

            verify(folioItemRepository, times(1)).save(any(FolioItem.class));
            verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("[TC-M5-021-08] Không có phòng nào đang ở → Night Audit chạy OK, không save FolioItem")
        void performNightAudit_NoOccupiedRooms_NoFolioItemSaved() {
            Employee staff = new Employee();
            staff.setId(10L);

            when(roomBookingDetailRepository.findAll()).thenReturn(new ArrayList<>());
            when(employeeRepository.findById(10L)).thenReturn(Optional.of(staff));

            folioService.performNightAudit(10L);

            verify(folioItemRepository, never()).save(any());
        }
    }
}
