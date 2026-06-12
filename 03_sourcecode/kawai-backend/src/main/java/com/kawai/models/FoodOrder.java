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

    @OneToMany(mappedBy = "foodOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<FoodOrderDetail> details;
}
