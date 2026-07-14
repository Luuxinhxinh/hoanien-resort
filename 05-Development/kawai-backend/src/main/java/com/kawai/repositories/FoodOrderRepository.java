package com.kawai.repositories;

import com.kawai.models.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {

    @Query("SELECT fo.orderStatus, COUNT(fo) FROM FoodOrder fo GROUP BY fo.orderStatus")
    List<Object[]> countByStatus();

    @Query("SELECT fo.paymentType, COUNT(fo) FROM FoodOrder fo GROUP BY fo.paymentType")
    List<Object[]> countByPaymentType();

    @Query("SELECT fo.orderType, COUNT(fo) FROM FoodOrder fo GROUP BY fo.orderType")
    List<Object[]> countByOrderType();

    @Query("SELECT SUM(d.priceAtOrder * d.quantity) FROM FoodOrderDetail d WHERE d.foodOrder.orderStatus = 'Completed'")
    BigDecimal totalCompletedRevenue();

    List<FoodOrder> findByBooking_Customer(com.kawai.models.Customer customer);

    @Query("SELECT DISTINCT fo FROM FoodOrder fo " +
           "LEFT JOIN fo.booking b " +
           "LEFT JOIN fo.roomBookingDetail rbd " +
           "LEFT JOIN rbd.roomBooking rb " +
           "WHERE b.customer = :customer " +
           "OR rbd.customer = :customer " +
           "OR rb.customer = :customer")
    List<FoodOrder> findByCustomer(@org.springframework.data.repository.query.Param("customer") com.kawai.models.Customer customer);

    @Query("SELECT SUM(d.priceAtOrder * d.quantity) FROM FoodOrderDetail d WHERE d.foodOrder.orderTime >= :startOfDay AND d.foodOrder.orderTime < :endOfDay AND d.foodOrder.orderStatus = 'Completed'")
    BigDecimal revenueOnDate(@org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay, @org.springframework.data.repository.query.Param("endOfDay") java.time.LocalDateTime endOfDay);

    @Query("SELECT SUM(d.priceAtOrder * d.quantity) FROM FoodOrderDetail d WHERE d.foodOrder.orderTime >= :start AND d.foodOrder.orderTime <= :end AND d.foodOrder.orderStatus = 'Completed'")
    BigDecimal revenueBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query("SELECT fo FROM FoodOrder fo WHERE fo.table.id = :tableId AND fo.isPaidInPos = false AND fo.orderStatus != 'Cancelled' ORDER BY fo.orderTime DESC")
    List<FoodOrder> findActiveOrdersByTable(@org.springframework.data.repository.query.Param("tableId") Long tableId);

    @Query("SELECT fo FROM FoodOrder fo WHERE LOWER(fo.orderType) IN ('room service', 'room-svc') ORDER BY fo.id DESC")
    List<FoodOrder> findRoomServiceOrders();

    List<FoodOrder> findByOrderTimeBetweenOrderByOrderTimeDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
}