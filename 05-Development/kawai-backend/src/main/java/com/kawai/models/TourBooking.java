package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity 
@Table(name="Tour_Bookings") 
@Data
@EqualsAndHashCode(callSuper=true)
public class TourBooking extends Booking {
    @ManyToOne @JoinColumn(name="schedule_id", nullable=false) private TourSchedule schedule;
    @Column(name="participant_count", nullable=false) private Integer participantCount;
    @Column(name="tour_charge", nullable=false) private java.math.BigDecimal tourCharge;
    
    @ManyToOne 
    @JoinColumn(name="room_booking_id", nullable=true) 
    private RoomBooking roomBooking;
    
    @ManyToOne 
    @JoinColumn(name="room_booking_detail_id") 
    private RoomBookingDetail roomBookingDetail;
}
