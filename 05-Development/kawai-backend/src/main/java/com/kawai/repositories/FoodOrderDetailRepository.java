package com.kawai.repositories;

import com.kawai.models.FoodOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface FoodOrderDetailRepository extends JpaRepository<FoodOrderDetail, Long> {

    @Query(value = "SELECT m.item_name FROM food_order_details d JOIN menu_items m ON d.menu_item_id = m.item_id GROUP BY d.menu_item_id ORDER BY SUM(d.quantity) DESC LIMIT 1", nativeQuery = true)
    String findTopDishName();
    
    @Query(value = "SELECT SUM(d.quantity) FROM food_order_details d JOIN menu_items m ON d.menu_item_id = m.item_id GROUP BY d.menu_item_id ORDER BY SUM(d.quantity) DESC LIMIT 1", nativeQuery = true)
    Integer findTopDishOrders();

    @Query(value = "SELECT m.item_name, m.category, SUM(d.quantity), SUM(d.price_at_order * d.quantity), m.price FROM food_order_details d JOIN menu_items m ON d.menu_item_id = m.item_id JOIN food_orders o ON d.order_id = o.order_id WHERE o.order_status = 'Completed' GROUP BY m.item_id", nativeQuery = true)
    java.util.List<Object[]> getFoodAnalytics();
}
