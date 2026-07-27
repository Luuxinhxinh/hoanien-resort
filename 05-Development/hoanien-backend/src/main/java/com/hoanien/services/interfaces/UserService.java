package com.hoanien.services.interfaces;

import com.hoanien.dto.CreateEmployeeDTO;
import com.hoanien.models.Account;
import com.hoanien.models.Employee;
import com.hoanien.models.Customer;
import com.hoanien.models.Role;
import java.util.Optional;

public interface UserService {
    Employee createEmployeeAccount(CreateEmployeeDTO dto);
    Customer createCustomerAccount(CreateEmployeeDTO dto);
    
    Employee updateEmployeeAccount(Long id, java.util.Map<String, String> payload);
    Customer updateCustomerAccount(Long id, java.util.Map<String, String> payload);
    
    void anonymizeCustomer(Long customerId);
    
    void updateCustomPermissions(Long accountId, java.util.List<String> permissions);

    // For permission reset
    Optional<Role> findRoleByName(String roleName);
    Account saveAccount(Account account);
    void deleteRole(Long roleId);
}
