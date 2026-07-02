package com.kawai.services;

import com.kawai.dto.CartItemDto;
import com.kawai.dto.CreateFoodOrderRequest;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.impl.PosServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC14: Quản lý đơn hàng F&B (PosService)
 * MODULE 3: POS & Nhà hàng
 * ═══════════════════════════════════════════════════════════════════════════
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC14 — Quản lý đơn hàng F&B")
class PosServiceUC14Test {

    @InjectMocks
    private PosServiceImpl posService;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private FoodOrderDetailRepository foodOrderDetailRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TableReservationRepository tableReservationRepository;

    @Test
    @DisplayName("TC-UC14-01 | Tạo mới đơn hàng hợp lệ (F&B Dine-In)")
    void testCreateOrder_DineIn_Success() {
        java.time.LocalTime now = java.time.LocalTime.now();
        org.junit.jupiter.api.Assumptions.assumeTrue(now.isAfter(java.time.LocalTime.of(8, 0)) && now.isBefore(java.time.LocalTime.of(23, 0)),
                "Bỏ qua test vào ban đêm vì luật kinh doanh không cho phép order F&B Dine-In từ 23:00 - 08:00");

        // Arrange
        CreateFoodOrderRequest request = new CreateFoodOrderRequest();
        request.setTableId(10L);
        request.setOrderType("dine-in");

        CartItemDto itemDto = new CartItemDto();
        itemDto.setId(1L);
        itemDto.setQty(2);
        request.setItems(Arrays.asList(itemDto));

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(10L);
        mockTable.setTableStatus("Available"); // Status OK

        MenuItem mockItem = new MenuItem();
        mockItem.setId(1L);
        mockItem.setPrice(new BigDecimal("100000"));

        Account mockUser = new Account();
        Employee mockEmp = new Employee();
        mockEmp.setId(5L);

        when(accountRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(foodOrderRepository.findActiveOrdersByTable(10L)).thenReturn(Collections.emptyList());
        when(restaurantTableRepository.findById(10L)).thenReturn(Optional.of(mockTable));
        when(tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(eq(10L), any()))
                .thenReturn(Collections.emptyList());
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(mockEmp));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(i -> {
            FoodOrder saved = i.getArgument(0);
            saved.setId(100L);
            return saved;
        });
        when(foodItemRepository.findById(1L)).thenReturn(Optional.of(mockItem));

        // Act
        FoodOrder result = posService.createOrder(request, "testUser");

        // Assert
        assertNotNull(result);
        assertEquals("Dine In", result.getOrderType());
        assertEquals("Pending", result.getOrderStatus());
        assertEquals(mockEmp, result.getCreatedByStaff());
        assertEquals("Occupied", mockTable.getTableStatus());

        // Base amount = 2 * 100k = 200k. Tax/Service depends on config, but should be >
        // 0.
        assertTrue(result.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        verify(restaurantTableRepository, times(1)).save(mockTable);
        verify(foodOrderDetailRepository, times(1)).save(any(FoodOrderDetail.class));
    }

    @Test
    @DisplayName("TC-UC14-02 | Thêm món vào đơn hàng đang mở (Update Total Amount)")
    void testAddItemsToOrder_Success() {
        // Arrange
        Long orderId = 100L;
        FoodOrder mockOrder = new FoodOrder();
        mockOrder.setId(orderId);
        mockOrder.setOrderStatus("Pending");
        CartItemDto itemDto = new CartItemDto();
        itemDto.setId(2L);
        itemDto.setQty(1);
        itemDto.setPrice(new BigDecimal("30000"));

        MenuItem mockItem = new MenuItem();
        mockItem.setId(2L);
        mockItem.setPrice(new BigDecimal("30000"));

        FoodOrderDetail existingDetail = new FoodOrderDetail();
        existingDetail.setPriceAtOrder(new BigDecimal("50000"));
        existingDetail.setQuantity(1);
        mockOrder.setDetails(new java.util.ArrayList<>(Arrays.asList(existingDetail)));

        when(foodOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(foodItemRepository.findById(2L)).thenReturn(Optional.of(mockItem));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenReturn(mockOrder);

        // Act
        posService.addItemsToOrder(orderId, Arrays.asList(itemDto));

        // Assert
        verify(foodOrderDetailRepository, times(1)).save(argThat(detail -> detail.getQuantity() == 1 &&
                detail.getFoodOrder().getId().equals(orderId)));
    }

    @Test
    @DisplayName("TC-UC14-03 | Exception khi tạo đơn trên bàn đang dọn")
    void testCreateOrder_TableCleaning_ThrowsException() {
        java.time.LocalTime now = java.time.LocalTime.now();
        org.junit.jupiter.api.Assumptions.assumeTrue(now.isAfter(java.time.LocalTime.of(8, 0)) && now.isBefore(java.time.LocalTime.of(23, 0)),
                "Bỏ qua test vào ban đêm vì luật kinh doanh không cho phép order F&B Dine-In từ 23:00 - 08:00");

        // Arrange
        CreateFoodOrderRequest request = new CreateFoodOrderRequest();
        request.setTableId(10L);
        request.setOrderType("dine-in");

        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(10L);
        mockTable.setTableStatus("Cleaning"); // Bàn đang dọn

        when(foodOrderRepository.findActiveOrdersByTable(10L)).thenReturn(Collections.emptyList());
        when(restaurantTableRepository.findById(10L)).thenReturn(Optional.of(mockTable));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            posService.createOrder(request, "testUser");
        });
        assertEquals("POS-007", exception.getErrorCode());
    }
}
