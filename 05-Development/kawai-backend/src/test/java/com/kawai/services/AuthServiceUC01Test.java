package com.kawai.services;

import com.kawai.models.Account;
import com.kawai.repositories.AccountRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC01 - Authentication Service")
public class AuthServiceUC01Test {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("TC-UC01-001 | Đăng nhập thành công, reset loginAttempts")
    void testLogin_Success() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("testuser");
        mockAccount.setPasswordHash("hashed_pass");
        mockAccount.setFailedLoginAttempts(3);

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches("correct_pass", "hashed_pass")).thenReturn(true);

        boolean result = authService.login("testuser", "correct_pass");

        assertTrue(result, "Đăng nhập thành công phải trả về true");
        assertEquals(0, mockAccount.getFailedLoginAttempts(), "Số lần đăng nhập sai phải được reset về 0");
        assertNull(mockAccount.getLockoutTime(), "Không được có thời gian khóa");
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    @DisplayName("TC-UC01-002 | Sai username (User không tồn tại)")
    void testLogin_WrongUsername() {
        when(accountRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        boolean result = authService.login("wronguser", "any_pass");

        assertFalse(result, "Username không tồn tại phải trả về false");
    }

    @Test
    @DisplayName("TC-UC01-003 | Sai password, tăng attempts và khóa nếu >= 5")
    void testLogin_WrongPassword_LockAccount() {
        Account mockAccount = new Account();
        mockAccount.setId(2L);
        mockAccount.setUsername("testuser");
        mockAccount.setPasswordHash("hashed_pass");
        mockAccount.setFailedLoginAttempts(4); // Lần này fail nữa là 5 -> Khóa

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches("wrong_pass", "hashed_pass")).thenReturn(false);

        boolean result = authService.login("testuser", "wrong_pass");

        assertFalse(result, "Đăng nhập sai phải trả về false");
        assertEquals(5, mockAccount.getFailedLoginAttempts(), "Số lần đăng nhập sai phải tăng lên 5");
        assertNotNull(mockAccount.getLockoutTime(), "Tài khoản phải bị khóa");
        assertTrue(mockAccount.getLockoutTime().isAfter(LocalDateTime.now()), "Lockout time phải ở trong tương lai");
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    @DisplayName("TC-UC01-004 | Account locked - Không cho phép đăng nhập")
    void testLogin_AccountLocked() {
        Account mockAccount = new Account();
        mockAccount.setId(3L);
        mockAccount.setUsername("lockeduser");
        mockAccount.setLockoutTime(LocalDateTime.now().plusMinutes(10)); // Đang bị khóa

        when(accountRepository.findByUsername("lockeduser")).thenReturn(Optional.of(mockAccount));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.login("lockeduser", "any_pass");
        });

        assertTrue(exception.getMessage().contains("bị khóa"));
        verify(passwordEncoder, never()).matches(anyString(), anyString()); // Chặn ngay từ đầu
    }
}
