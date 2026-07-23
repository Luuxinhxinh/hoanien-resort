package com.kawai.services;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.AuditLogRepository;
import com.kawai.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;

import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.WorkflowRepository;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC02 - Password Reset (AuthService)")
public class AuthServiceUC02Test {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("TC-UC02-001 | Request reset success")
    void requestReset_Success() {
        Customer mockCustomer = new Customer();
        mockCustomer.setEmail("test@gmail.com");
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockCustomer.setAccount(mockAccount);

        when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockCustomer));
        when(workflowRepository.findByTriggerEventAndIsActive(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        String token = authService.requestPasswordReset("test@gmail.com");

        assertNotNull(token);
        assertNotNull(mockAccount.getResetPasswordToken());
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    @DisplayName("TC-UC02-002 | Email not found")
    void requestReset_EmailNotFound() {
        when(customerRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            authService.requestPasswordReset("notfound@gmail.com");
        });
    }

    @Test
    @DisplayName("TC-UC02-003 | Reset success")
    void resetPassword_Success() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setResetPasswordToken("valid-token");
        mockAccount.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(10));
        mockAccount.setPasswordHash("oldHash");

        when(accountRepository.findByResetPasswordToken("valid-token")).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches("NewStrongPwd1!", "oldHash")).thenReturn(false);
        when(passwordEncoder.encode("NewStrongPwd1!")).thenReturn("newHash");

        boolean result = authService.resetPassword("valid-token", "NewStrongPwd1!");

        assertTrue(result);
        assertNull(mockAccount.getResetPasswordToken());
        assertEquals("newHash", mockAccount.getPasswordHash());
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    @DisplayName("TC-UC02-004 | Token expired")
    void resetPassword_TokenExpired() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setResetPasswordToken("expired-token");
        mockAccount.setResetPasswordExpiry(LocalDateTime.now().minusMinutes(10));

        when(accountRepository.findByResetPasswordToken("expired-token")).thenReturn(Optional.of(mockAccount));

        assertThrows(IllegalStateException.class, () -> {
            authService.resetPassword("expired-token", "NewStrongPwd1!");
        });
    }

    @Test
    @DisplayName("TC-UC02-005 | Weak password")
    void resetPassword_WeakPassword() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setResetPasswordToken("valid-token");
        mockAccount.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(10));

        when(accountRepository.findByResetPasswordToken("valid-token")).thenReturn(Optional.of(mockAccount));

        assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword("valid-token", "123");
        });
    }

    @Test
    @DisplayName("TC-UC02-006 | Mật khẩu mới trùng với mật khẩu cũ -> Ném IllegalArgumentException")
    void resetPassword_SameAsOldPassword() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setResetPasswordToken("valid-token");
        mockAccount.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(10));
        mockAccount.setPasswordHash("oldHash");

        when(accountRepository.findByResetPasswordToken("valid-token")).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches("OldPassword123!", "oldHash")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword("valid-token", "OldPassword123!");
        });
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-UC02-007 | Token không hợp lệ / không tồn tại trong DB -> Ném IllegalArgumentException")
    void resetPassword_InvalidToken() {
        when(accountRepository.findByResetPasswordToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword("invalid-token", "NewStrongPwd1!");
        });
    }

    @Test
    @DisplayName("TC-UC02-008 | Yêu cầu reset password cho Employee (khi không tìm thấy Customer)")
    void requestReset_EmployeeSuccess() {
        com.kawai.models.Employee mockEmp = new com.kawai.models.Employee();
        mockEmp.setEmail("staff@resort.com");
        Account mockAccount = new Account();
        mockAccount.setId(2L);
        mockEmp.setAccount(mockAccount);

        when(customerRepository.findByEmail("staff@resort.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("staff@resort.com")).thenReturn(Optional.of(mockEmp));
        when(workflowRepository.findByTriggerEventAndIsActive(anyString(), anyBoolean())).thenReturn(Collections.emptyList());

        String token = authService.requestPasswordReset("staff@resort.com");

        assertNotNull(token, "Token không được null khi tạo thành công cho Employee");
        assertNotNull(mockAccount.getResetPasswordToken());
        verify(accountRepository, times(1)).save(mockAccount);
    }
}
