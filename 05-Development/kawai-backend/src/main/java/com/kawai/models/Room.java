package com.kawai.models;

import jakarta.persistence.*;

import org.hibernate.envers.NotAudited;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "Rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;
    @Column(name = "room_number", unique = true, nullable = false)
    private String roomNumber;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private RoomCategory category;
    @Column(name = "room_status", nullable = false)
    private String roomStatus = "Vacant_Clean";
    @NotAudited
    @Column(name = "current_booking_detail_id")
    private Long currentBookingDetailId;

    // Getters / Setters
    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String v) {
        this.roomNumber = v;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory v) {
        this.category = v;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String v) {
        this.roomStatus = v;
    }

    public Long getCurrentBookingDetailId() {
        return currentBookingDetailId;
    }

    public void setCurrentBookingDetailId(Long v) {
        this.currentBookingDetailId = v;
    }
}