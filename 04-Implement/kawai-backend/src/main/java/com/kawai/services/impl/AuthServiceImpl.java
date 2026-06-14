package com.kawai.services.impl;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public boolean register(String username, String password, String email, String fullName, String gender, String phone) {
        if (accountRepository.existsByUsername(username) || customerRepository.existsByEmail(email)) {
            return false;
        }

        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("AUTH-001: Mật khẩu không đủ mạnh");
        }

        Role customerRole = roleRepository.findByRoleName("CUSTOMER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleName("CUSTOMER");
            return roleRepository.save(newRole);
        });

        Account account = new Account();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(customerRole);
        account.setIsActive(true);
        account = accountRepository.save(account);

        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setGender(gender);
        customer.setPhone(phone);
        customerRepository.save(customer);

        writeAuditLog(account, "REGISTER", "Accounts", account.getId(), null, "Registered account " + username);

        return true;
    }

    @Override
    @Transactional
    public boolean login(String username, String password) {
        Optional<Account> optAcc = accountRepository.findByUsername(username);
        if (optAcc.isEmpty()) {
            return false;
        }

        Account account = optAcc.get();

        if (account.getLockoutTime() != null && account.getLockoutTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("AUTH-005: Tài khoản bị khóa do nhập sai quá 5 lần");
        }

        if (passwordEncoder.matches(password, account.getPasswordHash())) {
            account.setFailedLoginAttempts(0);
            account.setLockoutTime(null);
            accountRepository.save(account);
            writeAuditLog(account, "LOGIN_SUCCESS", "Accounts", account.getId(), null, "Login successful");
            return true;
        } else {
            int attempts = account.getFailedLoginAttempts() != null ? account.getFailedLoginAttempts() : 0;
            attempts++;
            account.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                account.setLockoutTime(LocalDateTime.now().plusMinutes(15));
            }
            accountRepository.save(account);
            writeAuditLog(account, "LOGIN_FAIL", "Accounts", account.getId(), null, "Login failed. Attempt: " + attempts);
            return false;
        }
    }

    @Override
    @Transactional
    public String generate2FaOtp(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String otp = String.format("%06d", 100000 + random.nextInt(900000));
        account.setTwoFactorCode(otp);
        account.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(3));
        accountRepository.save(account);

        writeAuditLog(account, "GENERATE_OTP", "Accounts", account.getId(), null, "OTP generated: " + otp);
        return otp;
    }

    @Override
    @Transactional
    public boolean verify2FaOtp(String username, String code) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (account.getTwoFactorCode() == null || account.getTwoFactorExpiry() == null) {
            return false;
        }

        if (account.getTwoFactorExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("AUTH-003: OTP hết hạn");
        }

        if (account.getTwoFactorCode().equals(code)) {
            account.setTwoFactorCode(null);
            account.setTwoFactorExpiry(null);
            accountRepository.save(account);
            writeAuditLog(account, "VERIFY_OTP_SUCCESS", "Accounts", account.getId(), null, "OTP verified successfully");
            return true;
        } else {
            writeAuditLog(account, "VERIFY_OTP_FAIL", "Accounts", account.getId(), null, "OTP verification failed");
            return false;
        }
    }

    @Override
    @Transactional
    public String requestPasswordReset(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        Account account = customer.getAccount();
        if (account == null) {
            throw new IllegalArgumentException("Account not associated with email");
        }

        String token = UUID.randomUUID().toString();
        account.setResetPasswordToken(token);
        account.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(15));
        accountRepository.save(account);

        writeAuditLog(account, "RESET_PASSWORD_REQUEST", "Accounts", account.getId(), null, "Reset token: " + token);
        return token;
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Account account = accountRepository.findAll().stream()
                .filter(a -> token.equals(a.getResetPasswordToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Token reset hết hạn hoặc không hợp lệ"));

        if (account.getResetPasswordExpiry() == null || account.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token reset hết hạn hoặc không hợp lệ");
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("AUTH-001: Mật khẩu không đủ mạnh");
        }

        if (passwordEncoder.matches(newPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        String oldHash = account.getPasswordHash();
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setResetPasswordToken(null);
        account.setResetPasswordExpiry(null);
        accountRepository.save(account);

        writeAuditLog(account, "RESET_PASSWORD_SUCCESS", "Accounts", account.getId(), oldHash, "Password reset successfully");
        return true;
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }

    private void writeAuditLog(Account account, String action, String tableName, Long recordId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setAccount(account);
        log.setAction(action);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress("127.0.0.1");
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
