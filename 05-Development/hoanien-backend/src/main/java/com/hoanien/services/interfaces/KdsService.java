package com.hoanien.services.interfaces;

import com.hoanien.models.FoodOrder;
import com.hoanien.models.FoodOrderDetail;
import com.hoanien.models.TableReservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface KdsService {
    TableReservation createTableReservation(Long customerId, Long tableId, LocalDate date, LocalTime time, BigDecimal depositAmount);
    void checkAndCancelExpiredReservations();
    FoodOrder createFoodOrder(Long bookingDetailId, Long tableId, String orderType, List<FoodOrderDetail> items, Long staffId);
    void updateKitchenStatus(Long detailId, String status);
    void markItemUnavailable(Long itemId);
}
