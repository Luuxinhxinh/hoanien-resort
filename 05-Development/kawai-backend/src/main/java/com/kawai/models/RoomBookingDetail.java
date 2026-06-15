package com.kawai.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Room_Booking_Details")
public class RoomBookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private RoomBooking roomBooking;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private RoomCategory category;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "dependent_id")
    private Dependent dependent;

    @Column(name = "room_charge", nullable = false)
    private BigDecimal roomCharge;

    @Column(name = "detail_status", nullable = false)
    private String detailStatus = "Pending";

    @Column(name = "is_charge_to_room_allowed", nullable = false)
    private Boolean isChargeToRoomAllowed = true;

    @Column(name = "sub_credit_limit", nullable = false)
    private BigDecimal subCreditLimit = BigDecimal.ZERO;

    @Column(name = "billing_routing_strategy", nullable = false)
    private String billingRoutingStrategy = "BILL_TO_LEADER";

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public RoomBooking getRoomBooking() {
        return roomBooking;
    }

    public void setRoomBooking(RoomBooking v) {
        this.roomBooking = v;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory v) {
        this.category = v;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room v) {
        this.room = v;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer v) {
        this.customer = v;
    }

    public Dependent getDependent() {
        return dependent;
    }

    public void setDependent(Dependent v) {
        this.dependent = v;
    }

    public BigDecimal getRoomCharge() {
        return roomCharge;
    }

    public void setRoomCharge(BigDecimal v) {
        this.roomCharge = v;
    }

    public String getDetailStatus() {
        return detailStatus;
    }

    public void setDetailStatus(String v) {
        this.detailStatus = v;
    }

    public Boolean getIsChargeToRoomAllowed() {
        return isChargeToRoomAllowed;
    }

    public void setIsChargeToRoomAllowed(Boolean v) {
        this.isChargeToRoomAllowed = v;
    }

    public BigDecimal getSubCreditLimit() {
        return subCreditLimit;
    }

    public void setSubCreditLimit(BigDecimal v) {
        this.subCreditLimit = v;
    }

    public String getBillingRoutingStrategy() {
        return billingRoutingStrategy;
    }

    public void setBillingRoutingStrategy(String v) {
        this.billingRoutingStrategy = v;
    }
}