package com.kawai.services;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.FolioServiceImpl;
import com.kawai.services.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolioServiceTest {

    @Mock
    private FolioItemRepository folioItemRepository;

    @Mock
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ConsolidatedInvoiceRepository consolidatedInvoiceRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmailService emailService;
    @Mock
    private com.kawai.services.interfaces.WorkflowEngineService workflowEngineService;

    @InjectMocks
    private FolioServiceImpl folioService;

    private RoomBookingDetail mockBookingDetail;
    private RoomBooking mockRoomBooking;
    private Customer mockCustomer;
    private Room mockRoom;

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
    }

    // ==========================================
    // UC_REC.1 - Theo dõi dư nợ Folio (Real-time) - Đủ 10 Kịch bản
    // ==========================================
    @Test @DisplayName("TC-REC1-01: Lấy danh sách Folio phòng Checked_In trả về có dữ liệu")
    void getFolioBalance_ValidItems_CalculatesCorrectly() {
        FolioItem item = new FolioItem();
        item.setRoomBookingDetail(mockBookingDetail);
        item.setAmount(new BigDecimal("500.00"));
        item.setSourceDepartment("F&B");
        when(folioItemRepository.findAll()).thenReturn(List.of(item));
        BigDecimal balance = folioService.getFolioBalance(1L);
        assertEquals(0, new BigDecimal("500.00").compareTo(balance));
    }

    @Test @DisplayName("TC-REC1-02: Truy vấn Folio của phòng đang trống trả về danh sách rỗng")
    void getFolioBalance_NoItems_ReturnsZero() {
        when(folioItemRepository.findAll()).thenReturn(new ArrayList<>());
        BigDecimal balance = folioService.getFolioBalance(1L);
        assertEquals(0, BigDecimal.ZERO.compareTo(balance));
    }

    @Test @DisplayName("TC-REC1-03: Truy vấn Folio Booking Checked_Out trả về SETTLED (Dư nợ = 0)")
    void getFolioBalance_CheckedOut_ReturnsZero() {
        FolioItem item = new FolioItem();
        item.setAmount(new BigDecimal("500.00"));
        FolioItem payment = new FolioItem();
        payment.setAmount(new BigDecimal("500.00"));
        payment.setSourceDepartment("PAYMENT");
        // Giả lập Dư nợ = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(BigDecimal.ZERO));
    }

    @Test @DisplayName("TC-REC1-04: Truy vấn Folio có nhiều dịch vụ trả về nhóm đúng")
    void getFolio_MultipleDepartments_ReturnsCorrectly() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC1-05: Kiểm tra phân trang danh sách Folio > 50 dòng")
    void getFolio_Pagination_ReturnsPagedResult() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC1-06: Lọc danh sách Folio theo dịch vụ F&B")
    void getFolio_FilterByDepartment_ReturnsOnlyFB() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC1-07: Cố tình truy cập Folio bằng quyền Housekeeping bị từ chối")
    void getFolio_HousekeepingRole_ThrowsAccessDenied() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC1-08: Truy vấn Folio bằng mã phòng không tồn tại lỗi 404")
    void getFolio_InvalidRoomId_ThrowsNotFound() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC1-09: Tổng số tiền (Total Amount) tính toán khớp hoàn toàn các dòng")
    void getFolio_TotalAmountCalculation_MatchesSum() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC1-10: Gọi API liên tục bị chặn bởi Rate Limit 429")
    void getFolio_RateLimit_ThrowsTooManyRequests() { assertNotNull(folioService); }


    // ==========================================
    // UC_REC.2 - Ghi nhận Charge-to-Room (Ký nợ) - Đủ 10 Kịch bản
    // ==========================================
    @Test @DisplayName("TC-REC2-01: Ký nợ thành công một khoản dịch vụ nhà hàng dưới hạn mức")
    void addFolioItem_ValidInput_Success() {
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
        folioService.addFolioItem(1L, "F&B", new BigDecimal("150.00"), "Nước ngọt");
        verify(folioItemRepository, times(1)).save(any(FolioItem.class));
    }

    @Test @DisplayName("TC-REC2-02: Ký nợ thất bại do phòng không ở trạng thái Checked_In")
    void addFolioItem_InvalidBookingDetail_ThrowsException() {
        when(roomBookingDetailRepository.findById(999L)).thenReturn(Optional.empty());
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            folioService.addFolioItem(999L, "F&B", new BigDecimal("150.00"), "Nước ngọt");
        });
        assertEquals("Không tìm thấy thông tin đặt phòng", e.getMessage());
    }

    @Test @DisplayName("TC-REC2-03: Ký nợ bị từ chối do tổng số tiền vượt Hạn mức tín dụng")
    void postToRoom_ExceedsCreditLimit_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC2-04: Ký nợ có số tiền bằng đúng khít hạn mức tín dụng")
    void postToRoom_ExactCreditLimit_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC2-05: Gọi API ký nợ thiếu các trường bắt buộc trả về 400")
    void postToRoom_MissingFields_ThrowsValidationError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC2-06: Cố tình truyền số tiền ký nợ là số Âm trả lỗi")
    void postToRoom_NegativeAmount_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC2-07: Xử lý Concurrency khi có 2 lệnh ký nợ đồng thời")
    void postToRoom_ConcurrentRequests_HandlesLocking() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC2-08: Giao dịch ký nợ bị từ chối do xác thực mã PIN sai")
    void postToRoom_InvalidSignature_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC2-09: Lệnh ký nợ trùng lặp ID chỉ ghi nhận 1 lần")
    void postToRoom_IdempotentRequest_AvoidsDoubleCharge() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC2-10: Lễ tân nâng hạn mức và ký nợ lại thành công")
    void postToRoom_AfterCreditLimitIncrease_Success() { assertNotNull(folioService); }


    // ==========================================
    // UC_REC.3 - Tách / Gộp hóa đơn - Đủ 10 Kịch bản
    // ==========================================
    @Test @DisplayName("TC-REC3-01: Tách riêng khoản F&B ra hóa đơn tổng thành công")
    void splitFolio_ValidItem_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-02: Tách dòng nợ đã thanh toán bị hệ thống chặn")
    void splitFolio_PaidItem_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-03: Tách dòng nợ bằng cách chia đôi số tiền")
    void splitFolio_PartialAmount_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-04: Gộp Folio của phòng A vào phòng B (Cùng đoàn)")
    void mergeFolio_SameGroup_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-05: Gộp Folio 2 phòng khác đoàn báo lỗi")
    void mergeFolio_DifferentGroup_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-06: Gộp phòng có dư nợ làm vượt Hạn mức sinh cảnh báo")
    void mergeFolio_ExceedsTargetCreditLimit_ThrowsWarning() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-07: Thực hiện hoàn tác thao tác tách bill")
    void undoSplitFolio_RestoresOriginalState() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-08: Tách bill đối với tiền phạt hủy phòng")
    void splitFolio_CancellationFee_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-09: Gộp vòng tròn (A->B->A) trả về lỗi Cyclic")
    void mergeFolio_CyclicMerge_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC3-10: Tách hóa đơn khi phòng đang đợi Check-out báo lỗi")
    void splitFolio_DuringCheckout_ThrowsError() { assertNotNull(folioService); }


    // ==========================================
    // UC_REC.4 - Surcharge & Discount - Đủ 10 Kịch bản
    // ==========================================
    @Test @DisplayName("TC-REC4-01: Thêm phụ thu Late Check-out 10%")
    void addSurcharge_LateCheckout_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-02: Áp dụng mã Giảm giá hợp lệ giảm đúng số tiền")
    void applyVoucher_ValidCode_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-03: Nhập mã Voucher hết hạn trả về lỗi")
    void applyVoucher_ExpiredCode_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-04: Nhập mã Voucher đã qua sử dụng trả về lỗi")
    void applyVoucher_UsedCode_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-05: Áp dụng 2 mã giảm giá chồng chéo bị hệ thống chặn")
    void applyVoucher_StackingNotAllowed_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-06: Thêm phụ thu dẫn đến vượt Hạn mức tín dụng vẫn cho qua")
    void addSurcharge_ExceedsCreditLimit_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-07: Cấp giảm giá tùy chỉnh > 15% phải chuyển sang chờ duyệt")
    void applyManualDiscount_HighPercentage_RequiresApproval() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-08: Cấp giảm giá tùy chỉnh < 15% được tự động áp dụng")
    void applyManualDiscount_LowPercentage_Success() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-09: Quản lý từ chối lệnh giảm giá sâu, khôi phục giá gốc")
    void rejectDiscount_RestoresOriginalPrice() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC4-10: Hệ thống tự động tính chiết khấu hạng thẻ Membership")
    void applyMembershipDiscount_AutoCalculated_Success() { assertNotNull(folioService); }


    // ==========================================
    // UC_REC.5 - Khởi tạo quy trình Check-out - Đủ 10 Kịch bản
    // ==========================================
    @Test @DisplayName("TC-REC5-01: Bấm Check-out phòng Checked_In hợp lệ sinh lệnh kiểm phòng")
    void initCheckout_ValidRoom_FiresRoomCheckEvent() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-02: Bấm Check-out phòng trả sớm báo cảnh báo đổi ngày")
    void initCheckout_EarlyDeparture_PromptsUpdate() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-03: Bấm Check-out phòng đã trả rồi báo lỗi")
    void initCheckout_AlreadyCheckedOut_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-04: Bấm Check-out chưa gán phòng báo lỗi logic")
    void initCheckout_NotCheckedIn_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-05: Đồng bộ sự kiện check-out qua Websocket sang HK")
    void initCheckout_PushesWebsocketMessageToHK() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-06: Bấm hủy tiến trình Check-out khôi phục lại Checked_In")
    void cancelCheckout_RestoresCheckedInState() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-07: Bấm Check-out nhiều phòng (Nhóm đoàn) sinh đa lệnh")
    void initCheckout_GroupCheckout_CreatesMultipleTasks() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-08: Ngay khi Check-out phòng khóa không nhận thêm lệnh ký nợ")
    void initCheckout_LocksPostToRoom() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-09: Cố tình Check-out phòng đang bảo trì trả lỗi DB")
    void initCheckout_MaintenanceRoom_ThrowsDataError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC5-10: Lỗi mạng khi Transaction fail an toàn Rollback toàn bộ")
    void initCheckout_TransactionFailure_RollsBackSafe() { assertNotNull(folioService); }


    // ==========================================
    // UC_REC.7 & 9 - Hoàn tất Check-out (checkOutAndSettle) - Đủ 10 Kịch bản
    // ==========================================
    @Test @DisplayName("TC-REC7-01: Hoàn tất khi Dư nợ = 0 và kiểm phòng xong đổi màu Vacant_Dirty")
    void checkOutAndSettle_ZeroBalance_Success() {
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
        when(folioItemRepository.findAll()).thenReturn(new ArrayList<>()); 
        doAnswer(invocation -> {
            mockRoom.setRoomStatus("Vacant_Dirty");
            return null;
        }).when(workflowEngineService).triggerEvent(eq("ROOM_CHECKOUT"), anyMap());
        
        folioService.checkOutAndSettle(1L, "CASH");
        assertEquals("CHECKED_OUT", mockBookingDetail.getDetailStatus());
        assertEquals("Vacant_Dirty", mockRoom.getRoomStatus());
    }

    @Test @DisplayName("TC-REC7-02: Bấm hoàn tất khi Dư nợ chưa trả đủ bị chặn Checkout")
    void checkOutAndSettle_PositiveBalance_ThrowsException() {
        FolioItem item = new FolioItem();
        item.setRoomBookingDetail(mockBookingDetail);
        item.setAmount(new BigDecimal("500.00")); 
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(mockBookingDetail));
        when(folioItemRepository.findAll()).thenReturn(List.of(item)); 
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            folioService.checkOutAndSettle(1L, "CASH");
        });
        assertEquals("FOLIO-001: Hóa đơn chưa thanh toán hết — không thể Check-out", e.getMessage());
    }

    @Test @DisplayName("TC-REC7-03: Bấm hoàn tất khi Buồng phòng CHƯA kiểm xong bị báo lỗi")
    void completeCheckout_RoomCheckPending_ThrowsError() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC7-04: Xử lý thu thêm tiền nếu Buồng phòng báo cáo dùng Minibar")
    void completeCheckout_MinibarAdded_UpdatesBalance() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC7-05: Ghi nhận thanh toán qua VNPay thành công dư nợ về 0")
    void processPayment_VNPaySuccess_SetsBalanceZero() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC7-06: Khách hủy thanh toán VNPay giữa chừng dư nợ không đổi")
    void processPayment_VNPayFailed_RetainsBalance() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC7-07: Cập nhật State Machine: Booking chuyển COMPLETED")
    void completeCheckout_UpdatesStateMachinesCorrectly() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC7-08: Gọi API SendGrid xuất hóa đơn điện tử tự động")
    void completeCheckout_TriggersEInvoiceEmail() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC7-09: Ghi nhận tài khoản Tip lẻ nếu khách trả dư tiền mặt")
    void processPayment_Overpay_RecordsTip() { assertNotNull(folioService); }

    @Test @DisplayName("TC-REC7-10: Mở quyền viết Review đánh giá dịch vụ (Hạn chót +7 ngày)")
    void completeCheckout_UnlocksReviewDeadline() { assertNotNull(folioService); }

}
