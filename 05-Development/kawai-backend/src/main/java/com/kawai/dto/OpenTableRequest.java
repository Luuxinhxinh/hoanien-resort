package com.kawai.dto;

import lombok.Data;

@Data
public class OpenTableRequest {
    private Long tableId;
    private String customerName;
    private Integer partySize;
    private String notes;
}
