package com.kawai.services;

import com.kawai.models.FoodOrder;
import com.kawai.models.FoodOrderDetail;
import com.kawai.models.MenuItem;
import com.kawai.repositories.FoodItemRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.repositories.RestaurantTableRepository;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.services.impl.PosWebFacadeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC17 — Quản lý đơn Room Service")
class PosWebFacadeServiceUC17Test {

    @InjectMocks
    private PosWebFacadeServiceImpl posWebFacadeService;

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private TableReservationRepository tableReservationRepository;

    @Test
    @DisplayName("TC-UC17-01 | Filter đúng các đơn Room Service đang active (pending/preparing)")
    @SuppressWarnings("unchecked")
    void testGetRoomServiceData_FiltersActiveOrders() {
        // Arrange
        FoodOrder pendingOrder = new FoodOrder();
        pendingOrder.setId(1L);
        pendingOrder.setOrderStatus("Pending");
        pendingOrder.setOrderTime(LocalDateTime.now());

        FoodOrder servedOldOrder = new FoodOrder();
        servedOldOrder.setId(2L);
        servedOldOrder.setOrderStatus("Served");
        // Đơn served ngày hôm qua -> phải bị filter out
        servedOldOrder.setOrderTime(LocalDateTime.now().minusDays(1)); 

        when(foodOrderRepository.findRoomServiceOrders()).thenReturn(Arrays.asList(pendingOrder, servedOldOrder));

        // Act
        Map<String, Object> result = posWebFacadeService.getRoomServiceManagementData();

        // Assert
        List<Map<String, Object>> mappedOrders = (List<Map<String, Object>>) result.get("orders");
        assertEquals(1, mappedOrders.size(), "Chỉ 1 đơn hàng Pending của ngày hôm nay được trả về");
        assertEquals(1L, mappedOrders.get(0).get("id"));
        assertEquals(1L, result.get("pendingOrders"));
    }

    @Test
    @DisplayName("TC-UC17-02 | Tính toán ETA linh động (Dynamic SLA)")
    @SuppressWarnings("unchecked")
    void testGetRoomServiceData_CalculatesDynamicETA() {
        // Arrange
        FoodOrder order = new FoodOrder();
        order.setId(10L);
        order.setOrderStatus("Pending");
        order.setOrderTime(LocalDateTime.now());

        // Món chính (max prep = 20), 1 món
        MenuItem mainItem = new MenuItem();
        mainItem.setCategory("Món chính");
        FoodOrderDetail detail1 = new FoodOrderDetail();
        detail1.setMenuItem(mainItem);
        detail1.setQuantity(1);

        // Món phụ, 2 món (tổng = 3 món -> vượt 2 món -> +2 phút ETA)
        MenuItem drinkItem = new MenuItem();
        drinkItem.setCategory("Nước uống");
        FoodOrderDetail detail2 = new FoodOrderDetail();
        detail2.setMenuItem(drinkItem);
        detail2.setQuantity(2);

        order.setDetails(Arrays.asList(detail1, detail2));

        when(foodOrderRepository.findRoomServiceOrders()).thenReturn(Arrays.asList(order));

        // Act
        Map<String, Object> result = posWebFacadeService.getRoomServiceManagementData();

        // Assert
        List<Map<String, Object>> mappedOrders = (List<Map<String, Object>>) result.get("orders");
        assertEquals(1, mappedOrders.size());
        
        // Công thức: maxPrep (20) + (itemsCount (3) > 2 ? (3-2)*2 : 0) + 5 (di chuyển) = 27
        Integer etaMins = (Integer) mappedOrders.get(0).get("etaMins");
        assertEquals(27, etaMins, "ETA phải được tính đúng bằng 27 phút theo công thức");
    }
}
