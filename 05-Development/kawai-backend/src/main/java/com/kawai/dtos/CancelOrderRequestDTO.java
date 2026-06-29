package com.kawai.dtos;

import lombok.Data;

@Data
public class CancelOrderRequestDTO {
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String phoneNumber;
}
