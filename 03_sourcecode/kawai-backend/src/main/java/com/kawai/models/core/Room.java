package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Rooms") @Data
public class Room {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="room_id") private Long id;
    @Column(name="room_number", unique=true, nullable=false) private String roomNumber;
    @ManyToOne @JoinColumn(name="category_id", nullable=false) private RoomCategory category;
    @Column(name="room_status", nullable=false) private String roomStatus = "Vacant_Clean";
    @Column(name="current_booking_detail_id") private Long currentBookingDetailId;
}
