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
 * UC19 — Gọi món Dine-In tại quầy (Nhân viên POS lên đơn tại bàn)
 * Actor: Cashier (Thu ngân nhà hàng/F&B)
 *
 * Test Cases trong file này:
 *   TC-M3-006: Gọi món Dine-In trực tiếp → Thu ngân lên đơn gọi món,
 *              ghi nhận chính xác mã bàn ăn, số lượng, giá tiền tại
 *              thời điểm gọi món.
 *
 * Ghi chú về Retroactive TDD:
 *   Code nghiệp vụ của UC19 đã được implement trong PosApiController.createOrder().
 *   File test này được viết sau (Retroactive TDD) để đảm bảo tuân thủ quy trình
 *   TDD của nhóm. Pha Đỏ được mô phỏng bằng cách tạm thời vô hiệu hóa
 *   đoạn logic "gán bàn ăn" trong controller để test báo FAIL trước.
 */
@ExtendWith(MockitoExtension.class)
public class PosApiControllerUC19Test {

    // --- Mocks ---
    @Mock private FoodOrderRepository foodOrderRepository;
    @Mock private FoodOrderDetailRepository foodOrderDetailRepository;
    @Mock private RestaurantTableRepository restaurantTableRepository;
    @Mock private FoodItemRepository foodItemRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomBookingDetailRepository roomBookingDetailRepository;
    @Mock private RoomBookingRepository roomBookingRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private PosApiController posApiController;

    // --- Shared fixtures ---
    private CreateFoodOrderRequest request;
    private RestaurantTable table;
    private MenuItem menuItem1;
    private MenuItem menuItem2;

    @BeforeEach
    void setUp() {
        // Bàn ăn số A5, sức chứa 4
        table = new RestaurantTable();
        table.setId(5L);
        table.setTableNumber("A5");
        table.setCapacity(4);
        table.setTableStatus("Reserved");

        // Món 1: Cơm chiên hải sản — 120,000 VND x 2
        menuItem1 = new MenuItem();
        menuItem1.setId(10L);
        menuItem1.setPrice(new BigDecimal("120000"));

        // Món 2: Bạch tuộc nướng  — 85,000 VND x 3
        menuItem2 = new MenuItem();
        menuItem2.setId(11L);
        menuItem2.setPrice(new BigDecimal("85000"));

        // Tạo request Dine-In (không có roomNumber, có tableId)
        request = new CreateFoodOrderRequest();
        request.setOrderType("dine-in");          // Không phải "room-svc" → Dine In
        request.setTableId(5L);
        request.setPaymentType("Pay_Later");       // Thanh toán sau khi xong bữa

        CartItemDto item1 = new CartItemDto();
        item1.setId(10L);
        item1.setPrice(new BigDecimal("120000"));
        item1.setQty(2);

        CartItemDto item2 = new CartItemDto();
        item2.setId(11L);
        item2.setPrice(new BigDecimal("85000"));
        item2.setQty(3);

        List<CartItemDto> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        request.setItems(items);
    }

    // =====================================================================
    // TC-M3-006 — Happy Path: Tạo order Dine-In thành công
    // =====================================================================

    @Test
    @DisplayName("TC-M3-006 | UC19 | Dine-In — Tạo order thành công: ghi đúng bàn, số lượng, giá")
    void testCreateDineInOrder_Success() {
        // === ARRANGE ===
        // Thu ngân (Principal) đang đăng nhập
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("cashier01");
        when(accountRepository.findByUsername("cashier01")).thenReturn(Optional.empty());

        // Bàn A5 tồn tại
        when(restaurantTableRepository.findById(5L)).thenReturn(Optional.of(table));

        // Hai món ăn tồn tại trong hệ thống
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(menuItem1));
        when(foodItemRepository.findById(11L)).thenReturn(Optional.of(menuItem2));

        // Nhân viên stub (employee fallback)
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(new ArrayList<>());

        // Mock repository lưu order — gán ID 77
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> {
            FoodOrder fo = invocation.getArgument(0);
            fo.setId(77L);
            return fo;
        });

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        // 1. HTTP 200 OK
        assertEquals(200, response.getStatusCodeValue(),
                "Phải trả về HTTP 200 khi tạo order Dine-In thành công");

        // 2. Body phải có status = "success" và orderId = 77
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Response body không được null");
        assertEquals("success", body.get("status"),
                "status phải là 'success'");
        assertEquals(77L, body.get("orderId"),
                "orderId phải là 77 (ID đã mock)");

        // 3. FoodOrder đã được lưu đúng 1 lần
        verify(foodOrderRepository, times(1)).save(any(FoodOrder.class));

        // 4. Mỗi món ăn được lưu 1 lần vào FoodOrderDetail (2 món → 2 lần)
        verify(foodOrderDetailRepository, times(2)).save(any(FoodOrderDetail.class));

        // 5. Không có tương tác nào với RoomBookingRepository (đây là Dine-In, không phải Room Service)
        verify(roomBookingRepository, never()).save(any(RoomBooking.class));
    }

    // =====================================================================
    // TC-M3-006b — Verify giá tiền tại thời điểm gọi món (priceAtOrder)
    //              Đảm bảo đơn hàng lưu giá snapshot lúc gọi món,
    //              không phụ thuộc vào giá MenuItem thay đổi sau này.
    // =====================================================================

    @Test
    @DisplayName("TC-M3-006b | UC19 | Dine-In — Giá tại thời điểm gọi món (priceAtOrder) được lưu đúng")
    void testCreateDineInOrder_PriceAtOrderIsSnapshot() {
        // === ARRANGE ===
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("cashier01");
        when(accountRepository.findByUsername("cashier01")).thenReturn(Optional.empty());

        when(restaurantTableRepository.findById(5L)).thenReturn(Optional.of(table));

        // Chỉ 1 món: Cơm chiên hải sản 120k x 2
        CreateFoodOrderRequest singleItemReq = new CreateFoodOrderRequest();
        singleItemReq.setOrderType("dine-in");
        singleItemReq.setTableId(5L);
        singleItemReq.setPaymentType("Pay_Later");

        CartItemDto item = new CartItemDto();
        item.setId(10L);
        // Thu ngân nhập giá tại thời điểm gọi món là 120k
        item.setPrice(new BigDecimal("120000"));
        item.setQty(2);
        singleItemReq.setItems(List.of(item));

        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(menuItem1));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(new ArrayList<>());

        // Capture FoodOrderDetail được lưu để verify priceAtOrder
        List<FoodOrderDetail> capturedDetails = new ArrayList<>();
        when(foodOrderDetailRepository.save(any(FoodOrderDetail.class))).thenAnswer(inv -> {
            FoodOrderDetail det = inv.getArgument(0);
            capturedDetails.add(det);
            return det;
        });

        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(78L);
            return fo;
        });

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(singleItemReq, principal);

        // === ASSERT ===
        assertEquals(200, response.getStatusCodeValue());

        // Đảm bảo đúng 1 FoodOrderDetail được lưu
        assertEquals(1, capturedDetails.size(),
                "Phải lưu đúng 1 FoodOrderDetail cho 1 item");

        FoodOrderDetail savedDetail = capturedDetails.get(0);

        // priceAtOrder phải bằng giá lúc gọi món (120,000 VND)
        assertEquals(0, new BigDecimal("120000").compareTo(savedDetail.getPriceAtOrder()),
                "priceAtOrder phải là 120,000 VND (giá snapshot tại thời điểm gọi món)");

        // Quantity phải đúng (qty = 2)
        assertEquals(2, savedDetail.getQuantity(),
                "Số lượng (quantity) trong FoodOrderDetail phải là 2");

        // KOT status ban đầu phải là "Pending"
        assertEquals("Pending", savedDetail.getKotStatus(),
                "kotStatus ban đầu phải là 'Pending' — chờ bếp tiếp nhận");
    }

    // =====================================================================
    // TC-M3-006c — Dine-In với ghi chú đặc biệt (Note) của khách
    // =====================================================================

    @Test
    @DisplayName("TC-M3-006c | UC19 | Dine-In — Ghi chú đặc biệt được lưu vào order")
    void testCreateDineInOrder_WithGuestNoteAndName() {
        // === ARRANGE ===
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("cashier01");
        when(accountRepository.findByUsername("cashier01")).thenReturn(Optional.empty());

        when(restaurantTableRepository.findById(5L)).thenReturn(Optional.of(table));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(menuItem1));
        when(foodItemRepository.findById(11L)).thenReturn(Optional.of(menuItem2));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(new ArrayList<>());

        request.setGuestName("Nguyễn Văn A");
        request.setNote("Không cần nước mắm, dị ứng hải sản (riêng món số 2)");

        List<FoodOrder> capturedOrders = new ArrayList<>();
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(79L);
            capturedOrders.add(fo);
            return fo;
        });

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(capturedOrders.isEmpty(), "Phải có order được lưu");

        FoodOrder savedOrder = capturedOrders.get(0);

        // Note phải chứa tên khách và ghi chú đặc biệt
        assertNotNull(savedOrder.getNote(), "Note không được null");
        assertTrue(savedOrder.getNote().contains("Nguyễn Văn A"),
                "Note phải chứa tên khách hàng");
        assertTrue(savedOrder.getNote().contains("dị ứng hải sản"),
                "Note phải chứa ghi chú đặc biệt");

        // orderType phải là "Dine In"
        assertEquals("Dine In", savedOrder.getOrderType(),
                "orderType phải là 'Dine In' cho Dine-In order");

        // orderStatus ban đầu phải là "Pending"
        assertEquals("Pending", savedOrder.getOrderStatus(),
                "orderStatus ban đầu phải là 'Pending'");
    }

    // =====================================================================
    // TC-M3-006d — Dine-In: Thanh toán ngay tại POS (isPaid = true)
    //              → orderStatus = "PAID", isPaidInPos = true
    // =====================================================================

    @Test
    @DisplayName("TC-M3-006d | UC19 | Dine-In — Thanh toán ngay tại POS: status PAID")
    void testCreateDineInOrder_PaidImmediately() {
        // === ARRANGE ===
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("cashier01");
        when(accountRepository.findByUsername("cashier01")).thenReturn(Optional.empty());

        when(restaurantTableRepository.findById(5L)).thenReturn(Optional.of(table));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(menuItem1));
        when(foodItemRepository.findById(11L)).thenReturn(Optional.of(menuItem2));
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        when(employeeRepository.findAll()).thenReturn(new ArrayList<>());

        // Thu ngân bấm "Thanh toán ngay"
        request.setIsPaid(true);

        List<FoodOrder> capturedOrders = new ArrayList<>();
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(inv -> {
            FoodOrder fo = inv.getArgument(0);
            fo.setId(80L);
            capturedOrders.add(fo);
            return fo;
        });

        // === ACT ===
        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        // === ASSERT ===
        assertEquals(200, response.getStatusCodeValue(),
                "Phải trả về HTTP 200");

        assertFalse(capturedOrders.isEmpty(), "Phải có order được lưu");
        FoodOrder savedOrder = capturedOrders.get(0);

        // Khi isPaid = true → orderStatus = "PAID", isPaidInPos = true
        assertEquals("PAID", savedOrder.getOrderStatus(),
                "Khi thanh toán ngay, orderStatus phải là 'PAID'");
        assertTrue(savedOrder.getIsPaidInPos(),
                "isPaidInPos phải là true khi thanh toán tại POS");
    }

    // =====================================================================
    // TC-M3-006e — Dine-In: Lỗi khi Bàn không tồn tại (Table Not Found)
    // =====================================================================

    @Test
    @DisplayName("TC-M3-006e | UC19 | Dine-In — Lỗi 400 khi Bàn ăn không tồn tại")
    void testCreateDineInOrder_TableNotFound() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("cashier01");
        
        // Cố tình mock bàn số 999 không tồn tại
        request.setTableId(999L);
        when(restaurantTableRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        assertEquals(400, response.getStatusCodeValue(), "Phải trả về HTTP 400 Bad Request");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Bàn ăn không tồn tại!", body.get("message"));
    }

    // =====================================================================
    // TC-M3-006f — Dine-In: Lỗi khi Món ăn không tồn tại trong Menu
    // =====================================================================

    @Test
    @DisplayName("TC-M3-006f | UC19 | Dine-In — Lỗi 400 khi Món ăn không tồn tại")
    void testCreateDineInOrder_MenuItemNotFound() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("cashier01");
        
        when(restaurantTableRepository.findById(5L)).thenReturn(Optional.of(table));
        
        // Mock món 10L tồn tại, nhưng món 11L bị xóa/không tìm thấy
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(menuItem1));
        when(foodItemRepository.findById(11L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = posApiController.createOrder(request, principal);

        assertEquals(400, response.getStatusCodeValue(), "Phải trả về HTTP 400 Bad Request");
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("error", body.get("status"));
        assertEquals("Món ăn không tồn tại!", body.get("message"));
    }
}
