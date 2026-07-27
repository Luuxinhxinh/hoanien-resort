package com.hoanien.services;

import com.hoanien.exceptions.BusinessException;
import com.hoanien.models.FoodOrder;
import com.hoanien.models.RestaurantTable;
import com.hoanien.models.TableReservation;
import com.hoanien.repositories.FoodOrderRepository;
import com.hoanien.repositories.RestaurantTableRepository;
import com.hoanien.repositories.TableReservationRepository;
import com.hoanien.services.impl.PosServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC15 — Xác nhận thanh toán đơn hàng")
class PosServiceUC15Test {

    @InjectMocks
    private PosServiceImpl posService;

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private TableReservationRepository tableReservationRepository;

    @Test
    @DisplayName("TC-UC15-01 | Thanh toán hợp lệ -> isPaidInPos = true, bàn chuyển Cleaning")
    void testPayOrder_Success() {
        // Arrange
        Long orderId = 100L;
        FoodOrder mockOrder = new FoodOrder();
        mockOrder.setId(orderId);
        mockOrder.setOrderStatus("AWAITING_PAYMENT");
        
        RestaurantTable mockTable = new RestaurantTable();
        mockTable.setId(10L);
        mockTable.setTableStatus("Occupied");
        mockOrder.setTable(mockTable);

        TableReservation mockReservation = new TableReservation();
        mockReservation.setId(5L);
        mockReservation.setStatus("Seated");

        when(foodOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(tableReservationRepository.findByTable_IdAndReserveDateOrderByReserveTimeAsc(eq(10L), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(mockReservation));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenReturn(mockOrder);

        // Act
        FoodOrder result = posService.payOrder(orderId, "Pay_Now", null);

        // Assert
        assertTrue(result.getIsPaidInPos(), "Order isPaidInPos phải được set thành true");
        assertEquals("Pending", result.getOrderStatus(), "Trạng thái AWAITING_PAYMENT phải đổi thành Pending sau khi trả tiền");
        
        // Bàn phải được dọn
        assertEquals("Cleaning", mockTable.getTableStatus(), "Bàn phải được chuyển sang trạng thái Cleaning");
        assertNotNull(mockTable.getCleaningStartTime());
        verify(restaurantTableRepository, times(1)).save(mockTable);

        // Đặt bàn phải được hoàn tất
        assertEquals("Completed", mockReservation.getStatus(), "Đặt bàn phải chuyển thành Completed");
        assertNotNull(mockReservation.getEndTime());
        verify(tableReservationRepository, times(1)).save(mockReservation);
    }

    @Test
    @DisplayName("TC-UC15-02 | Đơn hàng không tồn tại -> Exception")
    void testPayOrder_OrderNotFound() {
        // Arrange
        Long orderId = 999L;
        when(foodOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            posService.payOrder(orderId, "Pay_Now", null);
        });

        assertEquals("POS-006", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("không tồn tại"));
    }
}
