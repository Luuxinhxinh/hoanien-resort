package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Food_Orders") @Data
public class FoodOrder {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="order_id") private Long id;
    @ManyToOne @JoinColumn(name="booking_id") private Booking booking;
    @ManyToOne @JoinColumn(name="room_booking_detail_id") private RoomBookingDetail roomBookingDetail;
    @ManyToOne @JoinColumn(name="table_id") private RestaurantTable table;
    @Column(name="order_type", nullable=false) private String orderType;
    @Column(name="order_status", nullable=false) private String orderStatus = "Pending";
    @Column(name="payment_type", nullable=false) private String paymentType;
    @Column(name="is_paid_in_pos", nullable=false) private Boolean isPaidInPos = false;
    @ManyToOne @JoinColumn(name="created_by_staff_id", nullable=false) private Employee createdByStaff;
    @ManyToOne @JoinColumn(name="kitchen_processed_by_id") private Employee kitchenProcessedBy;
    @Column(name="note", length=500) private String note;
    @Column(name="order_time", nullable=false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP") private java.time.LocalDateTime orderTime = java.time.LocalDateTime.now();
    @OneToMany(mappedBy = "foodOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<FoodOrderDetail> details;

    public java.math.BigDecimal getTotalAmount() {
        if (details == null || details.isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (FoodOrderDetail detail : details) {
            java.math.BigDecimal price = detail.getPriceAtOrder();
            if (price == null) {
                price = detail.getMenuItem() != null ? detail.getMenuItem().getPrice() : java.math.BigDecimal.ZERO;
            }
            if (price == null) {
                price = java.math.BigDecimal.ZERO;
            }
            total = total.add(price.multiply(java.math.BigDecimal.valueOf(detail.getQuantity() != null ? detail.getQuantity() : 1)));
        }
        
        // Add room service fee if applicable (5%)
        if ("RoomService".equalsIgnoreCase(orderType) || "Room Service".equalsIgnoreCase(orderType)) {
            java.math.BigDecimal fee = total.multiply(new java.math.BigDecimal("0.05"));
            total = total.add(fee);
        }
        
        return total;
    }
}
