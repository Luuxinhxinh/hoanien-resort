package com.kawai.services.custom;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import com.kawai.repositories.WorkflowRepository;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.AuditLogRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoleRepository;
import com.kawai.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceCustomTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testRegister_Success() {
        org.mockito.Mockito.lenient().when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        org.mockito.Mockito.lenient().when(customerRepository.existsByEmail("test@example.com")).thenReturn(false);
        org.mockito.Mockito.lenient().when(passwordEncoder.encode("Password123")).thenReturn("hashedPass");
        org.mockito.Mockito.lenient().when(roleRepository.findByRoleName("CUSTOMER NORMAL")).thenReturn(Optional.of(new Role()));

        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        org.mockito.Mockito.lenient().when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        boolean result = authService.register("testuser", "Password123", "test@example.com", "Test User", "Male", "0987654321");

        assertTrue(result);
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testRegister_PasswordTooWeak() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("testuser", "weak", "test@example.com", "Test User", "Male", "0987654321");
        });
    }

    @Test
    void testLogin_Success() {
        Account account = new Account();
        account.setUsername("testuser");
        account.setPasswordHash("hashedPass");
        account.setFailedLoginAttempts(3);

        org.mockito.Mockito.lenient().when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        org.mockito.Mockito.lenient().when(passwordEncoder.matches("Password123", "hashedPass")).thenReturn(true);

        boolean result = authService.login("testuser", "Password123");

        assertTrue(result);
        assertEquals(0, account.getFailedLoginAttempts());
        assertNull(account.getLockoutTime());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testLogin_BruteForceLockout() {
        Account account = new Account();
        account.setUsername("testuser");
        account.setPasswordHash("hashedPass");
        account.setFailedLoginAttempts(4);

        org.mockito.Mockito.lenient().when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        org.mockito.Mockito.lenient().when(passwordEncoder.matches("WrongPass", "hashedPass")).thenReturn(false);

        boolean result = authService.login("testuser", "WrongPass");

        assertFalse(result);
        assertEquals(5, account.getFailedLoginAttempts());
        assertNotNull(account.getLockoutTime());
        assertTrue(account.getLockoutTime().isAfter(LocalDateTime.now()));
    }

    @Test
    void test2FA_GenerateAndVerify() {
        Account account = new Account();
        account.setUsername("testuser");

        org.mockito.Mockito.lenient().when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        String otp = authService.generate2FaOtp("testuser");
        assertNotNull(otp);
        assertEquals(6, otp.length());

        // Verify success
        boolean verifyResult = authService.verify2FaOtp("testuser", otp);
        assertTrue(verifyResult);

        // Verify fail (expired)
        Account expiredAccount = new Account();
        expiredAccount.setUsername("testuser");
        expiredAccount.setTwoFactorCode(otp);
        expiredAccount.setTwoFactorExpiry(LocalDateTime.now().minusMinutes(1));
        org.mockito.Mockito.lenient().when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(expiredAccount));

        assertThrows(IllegalStateException.class, () -> {
            authService.verify2FaOtp("testuser", otp);
        });
    }

    @Test
    void testResetPassword_Success() {
        Account account = new Account();
        account.setUsername("testuser");
        account.setPasswordHash("oldHashed");
        account.setResetPasswordToken("reset-token");
        account.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(10));

        org.mockito.Mockito.lenient().when(accountRepository.findAll()).thenReturn(Collections.singletonList(account));
        org.mockito.Mockito.lenient().when(passwordEncoder.matches("NewPassword123", "oldHashed")).thenReturn(false);
        org.mockito.Mockito.lenient().when(passwordEncoder.encode("NewPassword123")).thenReturn("newHashed");

        boolean result = authService.resetPassword("reset-token", "NewPassword123");

        assertTrue(result);
        assertEquals("newHashed", account.getPasswordHash());
        assertNull(account.getResetPasswordToken());
    }
}
