package com.kawai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoDto {
    private String roomNumber;
    private String status;
    private boolean occupied;
    private String guestName;
    private BigDecimal limitRemaining;
}
