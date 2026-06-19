package com.kawai.controllers.api;

import com.kawai.dto.CartItemDto;
import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.models.*;
import com.kawai.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UC17 — Order Food Online (Room Service / E-Menu)
 * Actor: Customer (Khách đang check-in)
 *
 * Use Case Specification (SRS 2.1.17):
 *   - Primary Actor  : Customer
 *   - Secondary Actor: System / F&B Staff
 *   - Preconditions  : Khách đã đăng nhập, đang check-in, có hạn mức tín dụng còn lại > 0,
 *                      các món ăn trong menu đang available.
 *   - Postconditions : Food_Order & Food_Order_Detail được tạo, credit limit được trừ,
 *                      đơn hàng được chuyển tới F&B Staff và Kitchen.
 *
 * Test Cases trong file này:
 *   TC-M3-001  (Normal Flow)  : Đặt món Room Service thành công — Charge to Room.
 *   TC-M3-001b (AF1)          : Đặt nhiều món cùng lúc — Tổng tiền tính chính xác.
 *   TC-M3-001c (AF2)          : Khách thêm ghi chú đặc biệt — Note được lưu vào order.
 *   TC-M3-001d (E1)           : Hạn mức tín dụng không đủ — Từ chối đơn hàng (400).
 *   TC-M3-001e (E2)           : Phòng không tồn tại / chưa check-in — Từ chối (400).
 *   TC-M3-001f (E3)           : Món ăn không tồn tại trong Menu — Từ chối (400).
 *
 * Ghi chú về Retroactive TDD:
 *   Code nghiệp vụ của UC17 đã được implement trong PosApiController.createOrder().
 *   File test này được viết sau (Retroactive TDD) để đảm bảo tuân thủ quy trình TDD
 *   của nhóm. Pha Đỏ được mô phỏng bằng cách tạm thời comment out logic xử lý
 *   credit limit hoặc validation trong controller để test báo FAIL trước khi PASS.
 */
@ExtendWith(MockitoExtension.class)
public class PosApiControllerUC17Test {

    // ── Mocks ────────────────────────────────────────────────────────────
    @Mock private FoodOrderRepository foodOrderRepository;
    @Mock private FoodOrderDetailRepository foodOrderDetailRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock private FoodItemRepository foodItemRepository;
    @Mock private RoomBookingRepository roomBookingRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private RestaurantTableRepository restaurantTableRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private PosApiController posApiController;

    // ── Shared Fixtures ──────────────────────────────────────────────────
    private CreateFoodOrderRequest request;
    private RoomBooking roomBooking;
    private RoomBookingDetail roomBookingDetail;
    private Room room;
    private MenuItem menuItem1;
    private MenuItem menuItem2;

    @BeforeEach
    void setUp() {
        // Phòng 101 đang được khách check-in
        room = new Room();
        room.setRoomNumber("101");
        room.setCurrentBookingDetailId(1L);

        // RoomBooking có hạn mức tín dụng 500,000 VND
        roomBooking = new RoomBooking();
        roomBooking.setId(1L);
        roomBooking.setCreditLimit(new BigDecimal("500000"));

        // RoomBookingDetail liên kết phòng với booking
        roomBookingDetail = new RoomBookingDetail();
        roomBookingDetail.setId(1L);
        roomBookingDetail.setRoomBooking(roomBooking);

        // Món ăn 1: Gỏi cuốn tôm thịt — 100,000 VND
        menuItem1 = new MenuItem();
        menuItem1.setId(1L);
        menuItem1.setPrice(new BigDecimal("100000"));

        // Món ăn 2: Chả giò hải sản — 80,000 VND
        menuItem2 = new MenuItem();
        menuItem2.setId(2L);
        menuItem2.setPrice(new BigDecimal("80000"));

        // Request Room Service cơ bản: 1 món, Charge to Room
        request = new CreateFoodOrderRequest();
        request.setOrderType("room-svc");
        request.setRoomNumber("101");
        request.setPaymentType("CHARGE_TO_ROOM");

        CartItemDto item1 = new CartItemDto();
        item1.setId(1L);
        item1.setPrice(new BigDecimal("100000"));
        item1.setQty(2); // 2 phần → tổng 200,000 VND

        List<CartItemDto> items = new ArrayList<>();
        items.add(item1);
        request.setItems(items);
    }

    // =====================================================================
    // TC-M3-001 — Normal Flow: Đặt món Room Service thành công,
    //             Charge to Room, hạn mức tín dụng được trừ đúng.
    // Bám sát SRS UC-17 Normal Flow Steps 1–16.
    // =====================================================================

    @Test
    @DisplayName("TC-M3-001 | UC17 | Room Service — Tạo đơn thành công, hạn mức tín dụng được trừ đúng")
    void testCreateOrder_RoomService_ChargeToRoom_Success() {
        // === ARRANGE ===
        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(roomBookingDetail));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        when(accountRepository.findByUsername("customer01")).thenReturn(Optional.empty());
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(menuItem1));

        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(101L);
            return fo;
        });

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("customer01");

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        // Step 15 (SRS): System confirms successful order placement
        assertEquals(200, response.getStatusCodeValue(),
                "Phải trả về HTTP 200 OK khi tạo Room Service thành công");

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Response body không được null");
        assertEquals("success", body.get("status"),
                "status phải là 'success'");
        assertEquals(101L, body.get("orderId"),
                "orderId phải là 101 (ID đã mock)");

        // Postcondition (SRS): Food_Order lưu đúng 1 lần
        verify(foodOrderRepository, times(1)).save(any(FoodOrder.class));

        // Postcondition (SRS): Food_Order_Detail lưu đúng (1 dòng item)
        verify(foodOrderDetailRepository, times(1)).save(any(FoodOrderDetail.class));

        // Postcondition (SRS): Credit limit được tính lại và lưu vào DB
        // 2 x 100,000 = 200,000 + 5% phí = 210,000 → 500,000 - 210,000 = 290,000
        verify(roomBookingRepository, times(1)).save(roomBooking);
        assertEquals(0, new BigDecimal("290000").compareTo(roomBooking.getCreditLimit()),
                "Hạn mức tín dụng còn lại phải là 290,000 VND sau khi Charge to Room");
    }

    // =====================================================================
    // TC-M3-001b — Alternative Flow 1: Đặt nhiều món cùng lúc
    //              Tổng tiền được tính chính xác cho tất cả các dòng item.
    // Bám sát SRS UC-17 AF1: "Multiple Items Ordered".
    // =====================================================================

    @Test
    @DisplayName("TC-M3-001b | UC17 | Room Service — AF1: Đặt nhiều món — Tổng tiền và credit limit tính đúng")
    void testCreateOrder_MultipleItems_TotalCalculatedCorrectly() {
        // === ARRANGE ===
        // Thêm món thứ 2 vào request: 3 x 80,000 = 240,000
        CartItemDto item2 = new CartItemDto();
        item2.setId(2L);
        item2.setPrice(new BigDecimal("80000"));
        item2.setQty(3);
        request.getItems().add(item2);

        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(roomBookingDetail));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        when(accountRepository.findByUsername("customer01")).thenReturn(Optional.empty());
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(menuItem1));
        when(foodItemRepository.findById(2L)).thenReturn(Optional.of(menuItem2));

        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(102L);
            return fo;
        });

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("customer01");

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        assertEquals(200, response.getStatusCodeValue());

        // 2 dòng item → 2 FoodOrderDetail
        verify(foodOrderDetailRepository, times(2)).save(any(FoodOrderDetail.class));

        // Tổng subtotal: 200,000 + 240,000 = 440,000. Phí 5% = 22,000. Total = 462,000.
        // Credit limit còn: 500,000 - 462,000 = 38,000 VND
        verify(roomBookingRepository, times(1)).save(roomBooking);
        assertEquals(0, new BigDecimal("38000").compareTo(roomBooking.getCreditLimit()),
                "Hạn mức tín dụng còn lại phải là 38,000 VND sau khi Charge to Room nhiều món");
    }

    // =====================================================================
    // TC-M3-001c — Alternative Flow 2: Khách thêm ghi chú đặc biệt
    //              Note được đính kèm vào order và gửi đến bếp.
    // Bám sát SRS UC-17 AF2: "Special Request".
    // =====================================================================

    @Test
    @DisplayName("TC-M3-001c | UC17 | Room Service — AF2: Ghi chú đặc biệt được lưu vào order")
    void testCreateOrder_WithSpecialNote_NoteSavedToOrder() {
        // === ARRANGE ===
        request.setNote("Không hành, không tỏi. Dị ứng hải sản.");
        request.setGuestName("Nguyễn Thị B");

        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(roomBookingDetail));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        when(accountRepository.findByUsername("customer01")).thenReturn(Optional.empty());
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(menuItem1));

        List<FoodOrder> capturedOrders = new ArrayList<>();
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(103L);
            capturedOrders.add(fo);
            return fo;
        });

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("customer01");

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(capturedOrders.isEmpty(), "Phải có order được lưu");

        FoodOrder savedOrder = capturedOrders.get(0);

        // SRS AF2: Note phải chứa ghi chú đặc biệt của khách
        assertNotNull(savedOrder.getNote(), "Note không được null");
        assertTrue(savedOrder.getNote().contains("Dị ứng hải sản"),
                "Note phải chứa ghi chú dị ứng của khách");
        assertTrue(savedOrder.getNote().contains("Nguyễn Thị B"),
                "Note phải chứa tên khách");

        // Order type phải là Room Service
        assertEquals("Room Service", savedOrder.getOrderType(),
                "orderType phải là 'Room Service' cho đặt món online");

        // KOT status phải là Pending để gửi bếp
        assertEquals("Pending", savedOrder.getOrderStatus(),
                "orderStatus ban đầu phải là 'Pending'");
    }

    // =====================================================================
    // TC-M3-001d — Exception E1: Hạn mức tín dụng không đủ
    //              Hệ thống từ chối đơn hàng, không tạo order, không trừ hạn mức.
    // Bám sát SRS UC-17 E1: "Credit Limit Exceeded".
    // =====================================================================

    @Test
    @DisplayName("TC-M3-001d | UC17 | Room Service — E1: Hạn mức tín dụng không đủ, từ chối đơn (400)")
    void testCreateOrder_CreditLimitExceeded_OrderRejected() {
        // === ARRANGE ===
        // Đặt hạn mức chỉ còn 100,000 (nhỏ hơn 210,000 tổng cần trừ)
        roomBooking.setCreditLimit(new BigDecimal("100000"));

        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(roomBookingDetail));
        when(accountRepository.findByUsername("customer01")).thenReturn(Optional.empty());
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(menuItem1));

        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(99L);
            return fo;
        });

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("customer01");

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        // SRS E1: Hệ thống hiển thị thông báo lỗi, đơn hàng KHÔNG được tạo
        assertEquals(400, response.getStatusCodeValue(),
                "Phải trả về HTTP 400 Bad Request khi hạn mức tín dụng không đủ");

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"),
                "status phải là 'error'");
        assertTrue(body.get("message").toString().contains("Hạn mức tín dụng của phòng không đủ"),
                "Thông báo lỗi phải đề cập hạn mức tín dụng không đủ");

        // SRS Postcondition: Hạn mức tín dụng KHÔNG bị thay đổi khi lỗi
        verify(roomBookingRepository, never()).save(roomBooking);
        assertEquals(0, new BigDecimal("100000").compareTo(roomBooking.getCreditLimit()),
                "Hạn mức tín dụng phải giữ nguyên 100,000 VND (không bị trừ)");
    }

    // =====================================================================
    // TC-M3-001e — Exception E2: Phòng không tồn tại hoặc chưa check-in
    //              Hệ thống từ chối request, không tạo order.
    // Bám sát SRS UC-17 E2: "Room Not Eligible".
    // =====================================================================

    @Test
    @DisplayName("TC-M3-001e | UC17 | Room Service — E2: Phòng không tồn tại, hệ thống từ chối (400)")
    void testCreateOrder_RoomNotFound_OrderRejected() {
        // === ARRANGE ===
        // Phòng 999 không tồn tại trong database
        request.setRoomNumber("999");
        when(roomRepository.findByRoomNumber("999")).thenReturn(Optional.empty());
        when(accountRepository.findByUsername("customer01")).thenReturn(Optional.empty());

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("customer01");

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        // SRS E2: Hệ thống từ chối yêu cầu khi phòng không hợp lệ
        assertEquals(400, response.getStatusCodeValue(),
                "Phải trả về HTTP 400 Bad Request khi phòng không tồn tại");

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"),
                "status phải là 'error'");
        assertEquals("Phòng không tồn tại!", body.get("message"),
                "Thông báo lỗi phải là 'Phòng không tồn tại!'");

        // Food order KHÔNG được tạo
        verify(foodOrderRepository, never()).save(any(FoodOrder.class));
    }

    // =====================================================================
    // TC-M3-001f — Exception E3: Món ăn không còn tồn tại trong Menu
    //              Hệ thống từ chối đơn hàng, không lưu dữ liệu.
    // Bám sát SRS UC-17 E3: "Menu Item Unavailable".
    // =====================================================================

    @Test
    @DisplayName("TC-M3-001f | UC17 | Room Service — E3: Món ăn không tồn tại, hệ thống từ chối (400)")
    void testCreateOrder_MenuItemNotFound_OrderRejected() {
        // === ARRANGE ===
        when(roomRepository.findByRoomNumber("101")).thenReturn(Optional.of(room));
        when(roomBookingDetailRepository.findById(1L)).thenReturn(Optional.of(roomBookingDetail));
        when(accountRepository.findByUsername("customer01")).thenReturn(Optional.empty());

        // Món ăn ID 1 bị xóa khỏi database
        when(foodItemRepository.findById(1L)).thenReturn(Optional.empty());

        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(99L);
            return fo;
        });

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("customer01");

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        // SRS E3: Hệ thống yêu cầu chỉnh sửa đơn (từ chối lưu món không tồn tại)
        assertEquals(400, response.getStatusCodeValue(),
                "Phải trả về HTTP 400 Bad Request khi món ăn không tồn tại");

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"),
                "status phải là 'error'");
        assertEquals("Món ăn không tồn tại!", body.get("message"),
                "Thông báo lỗi phải là 'Món ăn không tồn tại!'");

        // FoodOrderDetail KHÔNG được lưu khi item không tồn tại
        verify(foodOrderDetailRepository, never()).save(any(FoodOrderDetail.class));
    }
}
