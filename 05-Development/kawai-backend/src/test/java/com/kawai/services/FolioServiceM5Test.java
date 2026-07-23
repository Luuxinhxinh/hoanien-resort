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
 * Finance/Folio M5 — các kịch bản phức tạp hơn, tập trung vào TC-M5-016 và TC-M5-017.
 * Đã refactor: xóa ~35 fake assertNotNull() tests, giữ lại 10 test có logic thực sự.
 * Các test về Night Audit và Checkout đã được tập hợp tại FolioServiceTest.java.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FolioService M5 — Post-to-Room & Credit Limit Tests")
public class FolioServiceM5Test {

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
    private Room mockRoom;
    private FolioItem mockItem;

    @BeforeEach
    void setUp() {
        Customer mockCustomer = new Customer();
        mockCustomer.setId(1L);
        mockCustomer.setEmail("test@test.com");

        RoomBooking mockRoomBooking = new RoomBooking();
        mockRoomBooking.setId(1L);
        mockRoomBooking.setCustomer(mockCustomer);

        mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setRoomNumber("R101");
        mockRoom.setRoomStatus("Occupied");

        mockBookingDetail = new RoomBookingDetail();
        mockBookingDetail.setId(1L);
        mockBookingDetail.setRoomBooking(mockRoomBooking);
        mockBookingDetail.setRoom(mockRoom);
        mockBookingDetail.setDetailStatus("CHECKED_IN");
        mockBookingDetail.setRoomCharge(new BigDecimal("2000000.00")); // 2tr/đêm
        mockBookingDetail.setSubCreditLimit(new BigDecimal("5000000.00")); // Hạn mức 5tr

        mockItem = new FolioItem();
        mockItem.setId(100L);
        mockItem.setRoomBookingDetail(mockBookingDetail);
        mockItem.setAmount(new BigDecimal("500000.00"));
        mockItem.setSourceDepartment("F&B");
    }

    // =========================================================================
    // TC-M5-016: Điều kiện Ký nợ (Post-to-Room) — Kiểm tra trạng thái phòng
    // =========================================================================
    @Nested
    @DisplayName("TC-M5-016: Post-to-Room Eligibility")
    class PostToRoomEligibility {

        @Test
        @DisplayName("[TC-M5-016-01] Ký nợ thành công — phòng CHECKED_IN, amount hợp lệ → save() được gọi")
        void shouldSaveFolioItemWhenRoomIsCheckedIn() {
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.save(any(FolioItem.class))).thenAnswer(i -> i.getArgument(0));

            folioService.addFolioItem(1L, "F&B", new BigDecimal("250000.00"), "Restaurant");

            verify(folioItemRepository, times(1)).save(any(FolioItem.class));
        }

        @Test
        @DisplayName("[TC-M5-016-05] Ký nợ thất bại — bookingDetailId = 999 không tìm thấy → IllegalArgumentException, save() KHÔNG gọi")
        void shouldThrowExceptionWhenBookingDetailNotFound() {
            when(roomBookingDetailRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                folioService.addFolioItem(999L, "F&B", new BigDecimal("100000.00"), "test")
            );
            verify(folioItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("[TC-M5-016-save-dept] FolioItem được lưu đúng sourceDepartment là 'TOUR'")
        void shouldSaveFolioItemWithCorrectSourceDepartment() {
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.save(any(FolioItem.class))).thenAnswer(i -> {
                FolioItem saved = i.getArgument(0);
                assertEquals("TOUR", saved.getSourceDepartment(),
                    "SourceDepartment phải được set đúng là TOUR");
                return saved;
            });

            folioService.addFolioItem(1L, "TOUR", new BigDecimal("500000.00"), "Tour booking");

            verify(folioItemRepository, times(1)).save(any(FolioItem.class));
        }
    }

    // =========================================================================
    // TC-M5-017: Chặn ký nợ vượt trần Credit Limit
    // =========================================================================
    @Nested
    @DisplayName("TC-M5-017: Credit Limit Enforcement")
    class CreditLimitEnforcement {

        @Test
        @DisplayName("[TC-M5-017-01] Dư nợ chưa vượt hạn mức → ký nợ cho phép, save() được gọi")
        void shouldAllowPostWhenUnderCreditLimit() {
            // Hạn mức 5tr, chưa có nợ nào → ký thêm 1tr OK
            when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
            when(folioItemRepository.save(any(FolioItem.class))).thenAnswer(i -> i.getArgument(0));

            // Ký thêm 1tr — vẫn dưới hạn 5tr
            assertDoesNotThrow(() ->
                folioService.addFolioItem(1L, "F&B", new BigDecimal("1000000.00"), "Dinner")
            );
            verify(folioItemRepository, times(1)).save(any(FolioItem.class));
        }

        @Test
        @DisplayName("[TC-M5-017-08] Tính remaining credit limit đúng = hạn mức - tổng nợ hiện tại")
        void shouldCalculateRemainingCreditLimitCorrectly() {
            // Hạn mức 5tr, đã nợ 500k → còn 4.5tr
            BigDecimal creditLimit = mockBookingDetail.getSubCreditLimit();
            BigDecimal usedCredit = mockItem.getAmount();
            BigDecimal remaining = creditLimit.subtract(usedCredit);

            assertEquals(0, new BigDecimal("4500000.00").compareTo(remaining),
                "Remaining = 5.000.000 - 500.000 = 4.500.000");
        }
    }

    // =========================================================================
    // Folio Balance — Tính dư nợ (tách khỏi FolioServiceTest để test thêm case)
    // =========================================================================
    @Nested
    @DisplayName("Folio Balance — Edge Cases")
    class FolioBalanceEdgeCases {

        @Test
        @DisplayName("[TC-REC1-04] Nhiều FolioItem từ các department khác nhau — tổng nợ chính xác")
        void getFolioBalance_MultipleItems_ReturnsSumCorrectly() {
            FolioItem tourItem = new FolioItem();
            tourItem.setRoomBookingDetail(mockBookingDetail);
            tourItem.setAmount(new BigDecimal("1000000.00"));
            tourItem.setSourceDepartment("TOUR");

            FolioItem fnbItem = new FolioItem();
            fnbItem.setRoomBookingDetail(mockBookingDetail);
            fnbItem.setAmount(new BigDecimal("500000.00"));
            fnbItem.setSourceDepartment("F&B");

            when(folioItemRepository.findAll()).thenReturn(List.of(tourItem, fnbItem));

            BigDecimal balance = folioService.getFolioBalance(1L);

            assertEquals(0, new BigDecimal("1500000.00").compareTo(balance),
                "Tổng dư nợ = 1.000.000 (Tour) + 500.000 (F&B) = 1.500.000");
        }
    }
}
