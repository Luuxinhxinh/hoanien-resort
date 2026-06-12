package com.kawai.services;

import com.kawai.dto.CreateEmployeeDTO;
import com.kawai.models.Account;
import com.kawai.models.Employee;
import com.kawai.models.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.RoleRepository;
import com.kawai.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testCreateEmployeeAccount_Success() {
        // Arrange
        CreateEmployeeDTO dto = new CreateEmployeeDTO();
        dto.setUsername("staff_new");
        dto.setPassword("password123");
        dto.setRoleId(2L); // 2L is for example ROLE_FB_STAFF
        dto.setFullName("Nguyen Van Staff");
        dto.setGender("MALE");
        dto.setCccd("012345678912");
        dto.setPhone("0987654321");
        dto.setEmail("staff@example.com");
        dto.setSalary(new BigDecimal("10000000"));

        Role mockRole = new Role();
        mockRole.setId(2L);
        mockRole.setRoleName("ROLE_FB_STAFF");

        when(roleRepository.findById(2L)).thenReturn(Optional.of(mockRole));
        when(accountRepository.existsByUsername("staff_new")).thenReturn(false);
        when(employeeRepository.existsByCccd("012345678912")).thenReturn(false);
        when(employeeRepository.existsByEmail("staff@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password123");

        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("staff_new");
        savedAccount.setPasswordHash("encoded_password123");
        savedAccount.setRole(mockRole);

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Employee savedEmployee = new Employee();
        savedEmployee.setId(1L);
        savedEmployee.setAccount(savedAccount);
        savedEmployee.setFullName("Nguyen Van Staff");
        savedEmployee.setGender("MALE");
        savedEmployee.setCccd("012345678912");
        savedEmployee.setPhone("0987654321");
        savedEmployee.setEmail("staff@example.com");
        savedEmployee.setSalary(new BigDecimal("10000000"));

        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        // Act
        Employee result = userService.createEmployeeAccount(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Nguyen Van Staff", result.getFullName());
        assertNotNull(result.getAccount());
        assertEquals("staff_new", result.getAccount().getUsername());
        assertEquals("ROLE_FB_STAFF", result.getAccount().getRole().getRoleName());
        
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
}
