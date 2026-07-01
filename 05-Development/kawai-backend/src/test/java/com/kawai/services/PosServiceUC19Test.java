package com.kawai.services;

import com.kawai.exceptions.BusinessException;
import com.kawai.models.FoodOrder;
import com.kawai.models.FoodOrderDetail;
import com.kawai.repositories.FoodOrderDetailRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.services.impl.PosServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JUnit Test — UC19: Xử lý lệnh bếp (PosService)
 * MODULE 3: POS & Nhà hàng
 * ═══════════════════════════════════════════════════════════════════════════
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC19 — Xử lý lệnh bếp")
class PosServiceUC19Test {

    @InjectMocks
    private PosServiceImpl posService;

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private FoodOrderDetailRepository foodOrderDetailRepository;

    @Test
    @DisplayName("TC-UC19-01 | Cập nhật trạng thái một món thành Preparing")
    void testUpdateOrderStatus_ItemPreparing_Success() {
        // Arrange
        FoodOrder mockOrder = new FoodOrder();
        mockOrder.setId(10L);
        mockOrder.setOrderStatus("Pending");

        FoodOrderDetail detail1 = new FoodOrderDetail();
        detail1.setId(1L);
        detail1.setKotStatus("Pending");
        detail1.setFoodOrder(mockOrder);

        mockOrder.setDetails(Arrays.asList(detail1));

        when(foodOrderRepository.findById(10L)).thenReturn(Optional.of(mockOrder));
        when(foodOrderRepository.save(any())).thenReturn(mockOrder);

        // Act
        posService.updateOrderStatus(10L, "Preparing");

        // Assert
        assertEquals("Preparing", detail1.getKotStatus());
        assertEquals("Preparing", mockOrder.getOrderStatus(),
                "Đơn hàng phải tự động chuyển sang Preparing khi có món bắt đầu làm");
        verify(foodOrderRepository, times(1)).save(mockOrder);
    }

    @Test
    @DisplayName("TC-UC19-02 | Cập nhật tất cả món thành Served -> Đơn chuyển Served")
    void testUpdateOrderStatus_AllItemsServed_OrderServed() {
        // Arrange
        FoodOrder mockOrder = new FoodOrder();
        mockOrder.setId(10L);
        mockOrder.setOrderStatus("Preparing");

        FoodOrderDetail detail1 = new FoodOrderDetail();
        detail1.setId(1L);
        detail1.setKotStatus("Ready");
        detail1.setFoodOrder(mockOrder);

        FoodOrderDetail detail2 = new FoodOrderDetail();
        detail2.setId(2L);
        detail2.setKotStatus("Served"); // Món khác đã Served
        detail2.setFoodOrder(mockOrder);

        mockOrder.setDetails(Arrays.asList(detail1, detail2));

        when(foodOrderRepository.findById(10L)).thenReturn(Optional.of(mockOrder));
        when(foodOrderRepository.save(any())).thenReturn(mockOrder);

        // Act
        posService.updateOrderStatus(10L, "Served"); // Update nốt detail1

        // Assert
        assertEquals("Served", detail1.getKotStatus());
        assertEquals("Served", mockOrder.getOrderStatus(),
                "Khi tất cả món đã Served, Đơn hàng phải chuyển sang Served");
        verify(foodOrderRepository, times(1)).save(mockOrder);
    }

    @Test
    @DisplayName("TC-UC19-03 | Exception khi cập nhật Order không tồn tại")
    void testUpdateOrderStatus_OrderNotFound() {
        // Arrange
        when(foodOrderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            posService.updateOrderStatus(99L, "Ready");
        });

        assertEquals("POS-006", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("tồn tại"));
    }
}
