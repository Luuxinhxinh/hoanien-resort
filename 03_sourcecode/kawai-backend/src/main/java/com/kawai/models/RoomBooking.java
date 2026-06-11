package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Entity @Table(name="Room_Bookings") @Data
public class RoomBooking extends Booking {
    @Column(name="check_in_date", nullable=false) private LocalDate checkInDate;
    @Column(name="check_out_date", nullable=false) private LocalDate checkOutDate;
    @Column(name="deposit_amount", nullable=false) private BigDecimal depositAmount;
    @Column(name="cancellation_deadline", nullable=false) private LocalDate cancellationDeadline;
    @Column(name="credit_limit", nullable=false) private BigDecimal creditLimit = new BigDecimal("5000000.00");
    @Column(name="personal_pin_hash", nullable=false) private String personalPinHash;
}
