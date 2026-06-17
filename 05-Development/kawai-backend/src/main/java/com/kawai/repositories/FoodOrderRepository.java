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

    @Query("SELECT COALESCE(SUM(d.priceAtOrder * d.quantity), 0) FROM FoodOrderDetail d WHERE d.foodOrder.orderStatus = 'Completed'")
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
}