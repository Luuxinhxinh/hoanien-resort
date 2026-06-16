package com.kawai.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Room_Guests")
public class RoomGuest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "detail_id", nullable = false)
    private RoomBookingDetail roomBookingDetail;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "dependent_id")
    private Dependent dependent;

    @Column(name = "guest_type", nullable = false)
    private String guestType;

    @Column(name = "is_primary_contact", nullable = false)
    private Boolean isPrimaryContact = false;

    // Getters / Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoomBookingDetail getRoomBookingDetail() {
        return roomBookingDetail;
    }

    public void setRoomBookingDetail(RoomBookingDetail roomBookingDetail) {
        this.roomBookingDetail = roomBookingDetail;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Dependent getDependent() {
        return dependent;
    }

    public void setDependent(Dependent dependent) {
        this.dependent = dependent;
    }

    public String getGuestType() {
        return guestType;
    }

    public void setGuestType(String guestType) {
        this.guestType = guestType;
    }

    public Boolean getIsPrimaryContact() {
        return isPrimaryContact;
    }

    public void setIsPrimaryContact(Boolean isPrimaryContact) {
        this.isPrimaryContact = isPrimaryContact;
    }
}
