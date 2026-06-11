package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
@Entity @Table(name="Room_Booking_Details") @Data
public class RoomBookingDetail {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="detail_id") private Long id;
    @ManyToOne @JoinColumn(name="booking_id", nullable=false) private RoomBooking roomBooking;
    @ManyToOne @JoinColumn(name="category_id", nullable=false) private RoomCategory category;
    @ManyToOne @JoinColumn(name="room_id") private Room room;
    
    @ManyToOne @JoinColumn(name="customer_id") private Customer customer;
    @ManyToOne @JoinColumn(name="dependent_id") private Dependent dependent;
    
    @Column(name="room_charge", nullable=false) private BigDecimal roomCharge;
    @Column(name="detail_status", nullable=false) private String detailStatus = "Pending";
    @Column(name="is_charge_to_room_allowed", nullable=false) private Boolean isChargeToRoomAllowed = true;
    @Column(name="sub_credit_limit", nullable=false) private BigDecimal subCreditLimit = BigDecimal.ZERO;
    @Column(name="billing_routing_strategy", nullable=false) private String billingRoutingStrategy = "BILL_TO_LEADER";
}
