package com.kawai.services;

import com.kawai.models.Booking;
import com.kawai.models.FolioItem;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.RoomCategory;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.services.impl.NightAuditServiceImpl;
import com.kawai.services.interfaces.NightAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD Test — UC24: Night Audit & Folio Aggregation (Module 5).
 *
 * Test cases được bao phủ:
 * <ul>
 * <li>TC-M5-001 — Folio hiển thị đúng danh sách nợ (BR-FB-01)</li>
 * <li>TC-M5-002 — Ghi nhận luồng tiền nhiều đợt (BR-FIN-06)</li>
 * <li>TC-M5-003 — Gom hóa đơn tổng = BigDecimal chính xác (BR-FIN-05)</li>
 * <li>TC-M5-004 — Night Audit 02:00 AM → cộng phí phòng ngày vào Folio
 * (BR-FIN-03)</li>
 * <li>TC-M5-005 — Night Audit chuyển Business Date lên 1 ngày (BR-FIN-07)</li>
 * </ul>
 *
 * <p>
 * TDD Phase: 🔴 RED — tất cả test phải FAIL trước khi implement
 * NightAuditServiceImpl.
 * NightAuditService hiện là @Mock (chưa có impl) → các assertion sẽ fail vì
 * mock trả null/default.
 *
 * @author Ngo Thi Ngoc Lan (MOD5)
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class NightAuditServiceUC24Test {

    // ── Repositories được mock ────────────────────────────────────────────────

    @Mock
    private FolioItemRepository folioItemRepository;

    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;

    /**
     * Service Under Test — @InjectMocks NightAuditServiceImpl để chạy real logic.
     * TDD GREEN: các test case sẽ PASS 100%.
     */
    @InjectMocks
    private NightAuditServiceImpl nightAuditService;

    // ── Fixtures ─────────────────────────────────────────────────────────────

    /** Chi tiết phòng đang OCCUPIED — phòng 101, phí 500,000 VNĐ/đêm */
    private RoomBookingDetail occupiedDetail;

    /** FolioItem ghi nợ tiền phòng */
    private FolioItem roomChargeItem;

    /** FolioItem ghi nợ tiền F&B */
    private FolioItem fbChargeItem;

    @BeforeEach
    void setUp() {
        // Tạo category phòng với giá 500,000 VNĐ/đêm
        RoomCategory category = new RoomCategory();
        category.setBasePrice(new BigDecimal("500000"));

        // Tạo RoomBookingDetail — phòng đang OCCUPIED
        occupiedDetail = new RoomBookingDetail();
        occupiedDetail.setId(1L);
        occupiedDetail.setDetailStatus("OCCUPIED");
        occupiedDetail.setRoomCharge(new BigDecimal("500000"));
        occupiedDetail.setCategory(category);

        // Tạo Booking mock
        com.kawai.models.RoomBooking booking = new com.kawai.models.RoomBooking();
        booking.setId(1L);
        occupiedDetail.setRoomBooking(booking);

        // FolioItem: Tiền phòng 500,000 VNĐ
        roomChargeItem = new FolioItem();
        roomChargeItem.setId(1L);
        roomChargeItem.setSourceDepartment("ROOM");
        roomChargeItem.setAmount(new BigDecimal("500000"));
        roomChargeItem.setDescription("Room Charge - Night 2026-06-14");
        roomChargeItem.setIsSettledSeparately(false);
        roomChargeItem.setBooking(booking);
        roomChargeItem.setRoomBookingDetail(occupiedDetail);

        // FolioItem: Tiền F&B 200,000 VNĐ
        fbChargeItem = new FolioItem();
        fbChargeItem.setId(2L);
        fbChargeItem.setSourceDepartment("FB");
        fbChargeItem.setAmount(new BigDecimal("200000"));
        fbChargeItem.setDescription("Restaurant Dinner");
        fbChargeItem.setIsSettledSeparately(false);
        fbChargeItem.setBooking(booking);
        fbChargeItem.setRoomBookingDetail(occupiedDetail);
    }

    // =========================================================================
    // TC-M5-001 — Folio hiển thị đúng danh sách nợ (UC24.1 / BR-FB-01)
    // =========================================================================

    /**
     * TC-M5-001: Folio của phòng OCCUPIED phải trả đúng danh sách tất cả FolioItem
     * chưa thanh toán.
     *
     * <p>
     * BR-FB-01: Chỉ phòng OCCUPIED mới có FolioItem hợp lệ.
     * Service phải gọi repository và trả về đúng danh sách.
     */
    @Test
    @DisplayName("TC-M5-001: getFolioItems() trả đúng danh sách FolioItem của phòng OCCUPIED")
    void tc_m5_001_getFolioItems_returnsCorrectListForOccupiedRoom() {
        // Arrange
        when(folioItemRepository.findByRoomBookingDetailId(1L))
                .thenReturn(List.of(roomChargeItem, fbChargeItem));

        // Act — sẽ fail vì NightAuditService.getFolioItems() chưa tồn tại
        List<FolioItem> result = nightAuditService.getFolioItems(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FolioItem::getSourceDepartment)
                .containsExactlyInAnyOrder("ROOM", "FB");
        verify(folioItemRepository, times(1)).findByRoomBookingDetailId(1L);
    }

    // =========================================================================
    // TC-M5-002 — Ghi nhận luồng tiền nhiều đợt (UC24.2 / BR-FIN-06)
    // =========================================================================

    /**
     * TC-M5-002: Khi Folio có nhiều FolioItem chưa thanh toán, tổng của chúng
     * phải được tính chính xác bằng BigDecimal (BR-FIN-06).
     *
     * <p>
     * Kịch bản: phòng nợ 500,000 (Room) + 200,000 (F&B) = 700,000 VNĐ.
     */
    @Test
    @DisplayName("TC-M5-002: calculateFolioBalance() = tổng nhiều khoản nợ BigDecimal chính xác")
    void tc_m5_002_calculateFolioBalance_sumOfMultipleItems() {
        // Arrange
        when(folioItemRepository.findByRoomBookingDetailId(1L))
                .thenReturn(List.of(roomChargeItem, fbChargeItem));

        // Act — sẽ fail vì NightAuditService.calculateFolioBalance() chưa tồn tại
        BigDecimal balance = nightAuditService.calculateFolioBalance(1L);

        // Assert: 500,000 + 200,000 = 700,000 (BR-FIN-05: BigDecimal, HALF_UP)
        assertThat(balance).isEqualByComparingTo(new BigDecimal("700000"));
    }

    /**
     * TC-M5-002b: Folio rỗng (không có khoản nào) → balance = 0.
     */
    @Test
    @DisplayName("TC-M5-002b: calculateFolioBalance() = 0 khi Folio không có item nào")
    void tc_m5_002b_calculateFolioBalance_returnsZeroWhenEmpty() {
        // Arrange
        when(folioItemRepository.findByRoomBookingDetailId(99L))
                .thenReturn(List.of());

        // Act
        BigDecimal balance = nightAuditService.calculateFolioBalance(99L);

        // Assert
        assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // =========================================================================
    // TC-M5-003 — Gom hóa đơn tổng chính xác (UC24.3 / BR-FIN-05)
    // =========================================================================

    /**
     * TC-M5-003: Tổng hóa đơn tổng hợp phải bằng sum(amount) của tất cả FolioItem
     * trong phòng, tính bằng BigDecimal với HALF_UP (BR-FIN-05).
     *
     * <p>
     * Kịch bản kiểm tra làm tròn: 3 khoản = 333.34 + 333.33 + 333.33 = 1,000.00
     */
    @Test
    @DisplayName("TC-M5-003: aggregateFolioTotal() làm tròn HALF_UP đúng chuẩn BR-FIN-05")
    void tc_m5_003_aggregateFolioTotal_bigDecimalRoundingIsCorrect() {
        // Arrange — 3 khoản với số lẻ để kiểm tra làm tròn
        FolioItem item1 = buildFolioItem(1L, "ROOM", "333.34");
        FolioItem item2 = buildFolioItem(2L, "FB", "333.33");
        FolioItem item3 = buildFolioItem(3L, "TOUR", "333.33");

        when(folioItemRepository.findByRoomBookingDetailId(1L))
                .thenReturn(List.of(item1, item2, item3));

        // Act — sẽ fail vì NightAuditService.aggregateFolioTotal() chưa tồn tại
        BigDecimal total = nightAuditService.aggregateFolioTotal(1L);

        // Assert: 333.34 + 333.33 + 333.33 = 1000.00
        assertThat(total).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    // =========================================================================
    // TC-M5-004 — Night Audit cộng phí phòng vào Folio (UC24.4 / BR-FIN-03)
    // =========================================================================

    /**
     * TC-M5-004: Khi Night Audit chạy, hệ thống phải tự động tạo FolioItem
     * với {@code sourceDepartment = "ROOM"} và {@code amount = roomCharge}
     * cho mỗi phòng đang OCCUPIED (BR-FIN-03).
     */
    @Test
    @DisplayName("TC-M5-004: runNightAudit() cộng phí phòng 500,000 vào Folio phòng OCCUPIED")
    void tc_m5_004_runNightAudit_postsRoomChargeToFolioForOccupiedRooms() {
        // Arrange
        when(roomBookingDetailRepository.findByDetailStatus("OCCUPIED"))
                .thenReturn(List.of(occupiedDetail));

        // Act — sẽ fail vì NightAuditService.runNightAudit() chưa tồn tại
        nightAuditService.runNightAudit(LocalDate.of(2026, 6, 14));

        // Assert: phải lưu 1 FolioItem mới với amount = 500,000 và department = ROOM
        ArgumentCaptor<FolioItem> captor = ArgumentCaptor.forClass(FolioItem.class);
        verify(folioItemRepository, atLeastOnce()).save(captor.capture());

        FolioItem savedItem = captor.getAllValues().stream()
                .filter(fi -> "ROOM".equals(fi.getSourceDepartment()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Không tìm thấy FolioItem loại ROOM được lưu"));

        assertThat(savedItem.getAmount())
                .isEqualByComparingTo(new BigDecimal("500000"));
        assertThat(savedItem.getSourceDepartment()).isEqualTo("ROOM");
        assertThat(savedItem.getIsSettledSeparately()).isFalse();
    }

    /**
     * TC-M5-004b: Phòng DIRTY hoặc VACANT không được cộng phí phòng — Night Audit
     * phải bỏ qua các phòng này.
     */
    @Test
    @DisplayName("TC-M5-004b: runNightAudit() bỏ qua phòng DIRTY/VACANT — không tạo FolioItem")
    void tc_m5_004b_runNightAudit_skipsNonOccupiedRooms() {
        // Arrange — không có phòng OCCUPIED nào
        when(roomBookingDetailRepository.findByDetailStatus("OCCUPIED"))
                .thenReturn(List.of());

        // Act
        nightAuditService.runNightAudit(LocalDate.of(2026, 6, 14));

        // Assert: không gọi save() cho FolioItem
        verify(folioItemRepository, never()).save(any(FolioItem.class));
    }

    // =========================================================================
    // TC-M5-005 — Night Audit chuyển Business Date lên 1 ngày (BR-FIN-07)
    // =========================================================================

    /**
     * TC-M5-005: Sau khi Night Audit hoàn tất, Business Date của hệ thống phải
     * tăng lên 1 ngày (BR-FIN-07: Khóa sổ sau Night Audit).
     *
     * <p>
     * Kịch bản: Chạy audit ngày 2026-06-14 → businessDate sau audit = 2026-06-15.
     */
    @Test
    @DisplayName("TC-M5-005: runNightAudit() chuyển Business Date từ 2026-06-14 → 2026-06-15")
    void tc_m5_005_runNightAudit_advancesBusinessDateByOneDay() {
        // Arrange
        LocalDate auditDate = LocalDate.of(2026, 6, 14);
        when(roomBookingDetailRepository.findByDetailStatus("OCCUPIED"))
                .thenReturn(List.of());

        // Act — sẽ fail vì NightAuditService.getNextBusinessDate() / runNightAudit()
        // chưa tồn tại
        nightAuditService.runNightAudit(auditDate);
        LocalDate nextDate = nightAuditService.getNextBusinessDate(auditDate);

        // Assert: ngày tiếp theo phải là auditDate + 1
        assertThat(nextDate).isEqualTo(LocalDate.of(2026, 6, 15));
    }

    /**
     * TC-M5-005b: Night Audit qua cuối tháng — chuyển ngày đúng
     * (2026-06-30 → 2026-07-01).
     */
    @Test
    @DisplayName("TC-M5-005b: runNightAudit() xử lý đúng khi cuối tháng (06-30 → 07-01)")
    void tc_m5_005b_runNightAudit_handlesMonthBoundaryCorrectly() {
        // Arrange
        LocalDate endOfMonth = LocalDate.of(2026, 6, 30);
        when(roomBookingDetailRepository.findByDetailStatus("OCCUPIED"))
                .thenReturn(List.of());

        // Act
        nightAuditService.runNightAudit(endOfMonth);
        LocalDate nextDate = nightAuditService.getNextBusinessDate(endOfMonth);

        // Assert
        assertThat(nextDate).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Builder nhanh cho FolioItem dùng trong test.
     */
    private FolioItem buildFolioItem(Long id, String department, String amount) {
        FolioItem item = new FolioItem();
        item.setId(id);
        item.setSourceDepartment(department);
        item.setAmount(new BigDecimal(amount));
        item.setDescription("Test charge - " + department);
        item.setIsSettledSeparately(false);
        item.setBooking(new Booking());
        item.setRoomBookingDetail(occupiedDetail);
        return item;
    }
}
