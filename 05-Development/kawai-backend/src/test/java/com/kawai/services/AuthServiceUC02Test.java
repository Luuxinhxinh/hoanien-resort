package com.kawai.services;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.repositories.WorkflowRepository;
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

        org.mockito.Mockito.lenient().when(customerRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockCustomer));

        String token = authService.requestPasswordReset("test@gmail.com");

        assertNotNull(token);
        assertNotNull(mockAccount.getResetPasswordToken());
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    @DisplayName("TC-UC02-002 | Email not found")
    void requestReset_EmailNotFound() {
        org.mockito.Mockito.lenient().when(customerRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());

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

        org.mockito.Mockito.lenient().when(accountRepository.findAll()).thenReturn(Collections.singletonList(mockAccount));
        org.mockito.Mockito.lenient().when(passwordEncoder.matches("NewStrongPwd1!", "oldHash")).thenReturn(false);
        org.mockito.Mockito.lenient().when(passwordEncoder.encode("NewStrongPwd1!")).thenReturn("newHash");

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

        org.mockito.Mockito.lenient().when(accountRepository.findAll()).thenReturn(Collections.singletonList(mockAccount));

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

        org.mockito.Mockito.lenient().when(accountRepository.findAll()).thenReturn(Collections.singletonList(mockAccount));

        assertThrows(IllegalArgumentException.class, () -> {
            authService.resetPassword("valid-token", "123");
        });
    }
}
