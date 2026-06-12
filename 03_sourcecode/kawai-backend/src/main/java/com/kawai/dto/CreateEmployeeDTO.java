package com.kawai.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateEmployeeDTO {
    private String username;
    private String password;
    private Long roleId;
    private String fullName;
    private String gender;
    private String cccd;
    private String phone;
    private String email;
    private BigDecimal salary;
}
