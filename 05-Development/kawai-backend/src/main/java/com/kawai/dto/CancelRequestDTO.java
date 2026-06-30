package com.kawai.dto;

import lombok.Data;

@Data
public class CancelRequestDTO {
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String phoneNumber;
}
