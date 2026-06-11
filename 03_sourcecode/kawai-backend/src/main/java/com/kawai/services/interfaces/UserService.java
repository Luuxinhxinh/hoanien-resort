package com.kawai.services.interfaces;

import com.kawai.dto.CreateEmployeeDTO;
import com.kawai.models.Employee;
import com.kawai.models.Customer;

public interface UserService {
    Employee createEmployeeAccount(CreateEmployeeDTO dto);
    Customer createCustomerAccount(CreateEmployeeDTO dto);
    
    Employee updateEmployeeAccount(Long id, java.util.Map<String, String> payload);
    Customer updateCustomerAccount(Long id, java.util.Map<String, String> payload);
}
