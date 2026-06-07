package com.kawai.models.core;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Entity @Table(name="Bookings") @Inheritance(strategy=InheritanceType.JOINED) @Data
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="booking_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @Column(name="booking_date", nullable=false) private LocalDate bookingDate;
    @Column(name="total_price", nullable=false) private BigDecimal totalPrice;
    @Column(name="booking_status", nullable=false) private String bookingStatus = "Pending";
    @Column(name="booking_source", nullable=false) private String bookingSource = "Direct_Web";
    @Column(nullable=false) private Integer version = 1;
}
