package com.kawai.services.interfaces;

import com.kawai.dto.CreateEmployeeDTO;
import com.kawai.models.Account;
import com.kawai.models.Employee;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import java.util.Optional;

public interface UserService {
    Employee createEmployeeAccount(CreateEmployeeDTO dto);
    Customer createCustomerAccount(CreateEmployeeDTO dto);
    
    Employee updateEmployeeAccount(Long id, java.util.Map<String, String> payload);
    Customer updateCustomerAccount(Long id, java.util.Map<String, String> payload);
    
    void updateCustomPermissions(Long accountId, java.util.List<String> permissions);

    // For permission reset
    Optional<Role> findRoleByName(String roleName);
    Account saveAccount(Account account);
    void deleteRole(Long roleId);
}
