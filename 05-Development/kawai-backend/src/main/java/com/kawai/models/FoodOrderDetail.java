package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Food_Order_Details") @Data
public class FoodOrderDetail {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="detail_id") private Long id;
    @ManyToOne @JoinColumn(name="order_id", nullable=false) private FoodOrder foodOrder;
    @ManyToOne @JoinColumn(name="menu_item_id", nullable=false) private MenuItem menuItem;
    @Column(nullable=false) private Integer quantity;
    @Column(name="price_at_order", nullable=false) private BigDecimal priceAtOrder;
    @Column(name="kot_status", nullable=false) private String kotStatus = "Pending";

    public BigDecimal getTotalPrice() {
        if (priceAtOrder == null) return BigDecimal.ZERO;
        return priceAtOrder.multiply(new BigDecimal(quantity != null ? quantity : 0));
    }
}
