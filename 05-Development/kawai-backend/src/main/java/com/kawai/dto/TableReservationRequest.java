package com.kawai.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TableReservationRequest {
    private Long tableId;
    private LocalDate reserveDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer partySize;
    private String specialRequests;
}
