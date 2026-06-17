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
    @JoinColumn(name = "room_booking_id", nullable = false)
    private RoomBooking roomBooking;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private RoomCategory category;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "room_charge", nullable = false)
    private BigDecimal roomCharge;

    @Column(name = "detail_status", nullable = false)
    private String detailStatus = "Pending";

    @Column(name = "bed_preference", nullable = false)
    private String bedPreference = "KING_SIZE";

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @Column(name = "is_charge_to_room_allowed", nullable = false)
    private Boolean isChargeToRoomAllowed = true;

    @Column(name = "sub_credit_limit", nullable = false)
    private BigDecimal subCreditLimit = BigDecimal.ZERO;

    @Column(name = "billing_routing_strategy", nullable = false)
    private String billingRoutingStrategy = "BILL_TO_LEADER";

    @Column(name = "number_of_adults")
    private Integer numberOfAdults;

    @Column(name = "number_of_children")
    private Integer numberOfChildren;

    @Column(name = "extra_surcharge")
    private BigDecimal extraSurcharge;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

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

    public String getBedPreference() {
        return bedPreference;
    }

    public void setBedPreference(String v) {
        this.bedPreference = v;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String v) {
        this.specialRequests = v;
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

    public Integer getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(Integer numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public BigDecimal getExtraSurcharge() {
        return extraSurcharge;
    }

    public void setExtraSurcharge(BigDecimal extraSurcharge) {
        this.extraSurcharge = extraSurcharge;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}