package com.kawai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
@Entity @Table(name="Table_Reservations") @Data
public class TableReservation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="reservation_id") private Long id;
    @ManyToOne @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @ManyToOne @JoinColumn(name="table_id", nullable=false) private RestaurantTable table;
    @Column(name="reserve_date", nullable=false) private LocalDate reserveDate;
    @Column(name="reserve_time", nullable=false) private LocalTime reserveTime;
    @Column(name="end_time") private LocalTime endTime;
    @Column(name="deposit_amount", nullable=false) private BigDecimal depositAmount = BigDecimal.ZERO;
    @Column(nullable=false) private String status = "Pending";
    @Column(name="party_size") private Integer partySize;
    @Column(name="special_requests", length=500) private String specialRequests;
}
