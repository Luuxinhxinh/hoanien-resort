package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name="Tour_Bookings") @Data
public class TourBooking extends Booking {
    @ManyToOne @JoinColumn(name="schedule_id", nullable=false) private TourSchedule schedule;
    @Column(name="participant_count", nullable=false) private Integer participantCount;
    @Column(name="is_walk_in_tour", nullable=false) private Boolean isWalkInTour = false;
}
