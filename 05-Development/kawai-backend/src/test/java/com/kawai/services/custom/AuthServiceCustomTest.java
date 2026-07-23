package com.kawai.services.custom;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.AuditLogRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoleRepository;
import com.kawai.repositories.MembershipTierRepository;
import com.kawai.services.impl.AuthServiceImpl;
import com.kawai.models.MembershipTier;
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

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testRegister_Success() {
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        lenient().when(passwordEncoder.encode("Password123")).thenReturn("hashedPass");
        lenient().when(roleRepository.findByRoleName("CUSTOMER NORMAL")).thenReturn(Optional.of(new Role()));
        lenient().when(membershipTierRepository.findByTierNameIgnoreCase(anyString())).thenReturn(Optional.of(new MembershipTier()));

        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

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

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Password123", "hashedPass")).thenReturn(true);

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

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("WrongPass", "hashedPass")).thenReturn(false);

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

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

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
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(expiredAccount));

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

        when(accountRepository.findByResetPasswordToken("reset-token")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("NewPassword123", "oldHashed")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newHashed");

        boolean result = authService.resetPassword("reset-token", "NewPassword123");

        assertTrue(result);
        assertEquals("newHashed", account.getPasswordHash());
        assertNull(account.getResetPasswordToken());
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsException() {
        when(accountRepository.findByUsername("existinguser")).thenReturn(Optional.of(new Account()));

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("existinguser", "Password123", "new@example.com", "Name", "Male", "0912345678");
        });
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        when(accountRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        
        Customer existingCustomer = new Customer();
        Account activeAccount = new Account();
        activeAccount.setIsActive(true);
        existingCustomer.setAccount(activeAccount);

        when(customerRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingCustomer));

        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("newuser", "Password123", "existing@example.com", "Name", "Male", "0912345678");
        });
        verify(accountRepository, never()).save(any());
    }

    @Test
    void test2FA_WrongOtp_ReturnsFalse() {
        Account account = new Account();
        account.setUsername("testuser");
        account.setTwoFactorCode("123456");
        account.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        boolean result = authService.verify2FaOtp("testuser", "654321");

        assertFalse(result, "Nhập sai mã OTP 2FA phải trả về false");
    }

    @Test
    void test2FA_GenerateOtp_UserNotFound_ThrowsException() {
        when(accountRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            authService.generate2FaOtp("unknown");
        });
    }
}
