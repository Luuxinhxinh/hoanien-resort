package com.kawai.services.impl;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.AuthService;
import com.kawai.services.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private com.kawai.services.interfaces.WorkflowEngineService workflowEngineService;

    @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TourEmailService tourEmailService;

    private final Random random = new Random();

    @Override
    @Transactional
    public boolean register(String username, String password, String email, String fullName, String gender,
            String phone) {

        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        Account accountToUse = null;
        Customer customerToUse = null;

        Optional<Account> existingAccOpt = accountRepository.findByUsername(username);
        if (existingAccOpt.isPresent()) {
            Account acc = existingAccOpt.get();
            if (acc.getIsActive() != null && acc.getIsActive()) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
            } else {
                accountToUse = acc;
            }
        }
        if (phone != null && !phone.trim().isEmpty() && !com.kawai.utils.ValidationUtils.isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone number format (Must be 10 digits starting with 0)");
        }

        Optional<Customer> existingCustOpt = customerRepository.findByEmail(email);
        if (existingCustOpt.isPresent()) {
            Customer cust = existingCustOpt.get();
            Account custAcc = cust.getAccount();
            if (custAcc != null && custAcc.getIsActive() != null && custAcc.getIsActive()) {
                throw new IllegalArgumentException("Email này đã được sử dụng!");
            } else if (custAcc != null && (custAcc.getIsActive() == null || !custAcc.getIsActive())) {
                if (accountToUse != null && !custAcc.getId().equals(accountToUse.getId())) {
                    throw new IllegalArgumentException("Email này đang chờ xác thực cho một tên đăng nhập khác!");
                }
                accountToUse = custAcc;
                customerToUse = cust;
            }
        }

        if (accountToUse == null) {
            accountToUse = new Account();
            accountToUse.setUsername(username);
            Role customerRole = roleRepository.findByRoleName("CUSTOMER NORMAL")
                    .or(() -> roleRepository.findByRoleName("CUSTOMER"))
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleName("CUSTOMER");
                        return roleRepository.save(newRole);
                    });
            accountToUse.setRole(customerRole);
        } else {
            accountToUse.setUsername(username);
        }

        accountToUse.setPasswordHash(passwordEncoder.encode(password));
        accountToUse.setIsActive(false); // Chờ xác thực OTP
        accountToUse = accountRepository.save(accountToUse);

        if (customerToUse == null) {
            customerToUse = customerRepository.findByAccount_Username(accountToUse.getUsername())
                    .orElse(new Customer());
        }

        customerToUse.setAccount(accountToUse);
        customerToUse.setFullName(fullName != null && !fullName.trim().isEmpty() ? fullName : username);
        customerToUse.setEmail(email);
        customerToUse.setGender(gender != null ? gender : "Other");
        customerToUse.setPhone(phone != null && !phone.trim().isEmpty() ? phone : "0000000000");
        customerRepository.save(customerToUse);

        writeAuditLog(accountToUse, "REGISTER", "Accounts", accountToUse.getId(), null,
                "Registered account " + username);

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

        int maxAttempts = 5; // default
        int lockMinutes = 15; // default

        try {
            Optional<Workflow> activeWfOpt = workflowRepository.findByTriggerEventAndIsActive("ACCOUNT_SECURITY", true)
                    .stream().findFirst();
            if (activeWfOpt.isPresent()) {
                Workflow wf = activeWfOpt.get();
                if (wf.getConditionsJson() != null && !wf.getConditionsJson().trim().isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> conds = mapper.readValue(wf.getConditionsJson(),
                            new TypeReference<Map<String, Object>>() {
                            });
                    if (conds.containsKey("failed_login_attempts")) {
                        maxAttempts = Integer.parseInt(conds.get("failed_login_attempts").toString());
                    }
                    if (conds.containsKey("lockout_time_minutes")) {
                        lockMinutes = Integer.parseInt(conds.get("lockout_time_minutes").toString());
                    }
                }
            }
        } catch (Exception e) {
            // fallback
        }

        if (account.getLockoutTime() != null && account.getLockoutTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("AUTH-005: Tài khoản bị khóa do nhập sai quá " + maxAttempts + " lần");
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
            if (attempts >= maxAttempts) {
                account.setLockoutTime(LocalDateTime.now().plusMinutes(lockMinutes));
                account.setIsActive(false);
            }
            accountRepository.save(account);
            writeAuditLog(account, "LOGIN_FAIL", "Accounts", account.getId(), null,
                    "Login failed. Attempt: " + attempts);
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

        // Gửi email OTP xác nhận đăng ký qua Workflow Engine nếu có cấu hình, ngược lại dùng fallback
        Customer customer = customerRepository.findByAccount_Username(username).orElse(null);
        if (customer != null && customer.getEmail() != null) {
            boolean hasActiveWorkflow = workflowRepository.findByTriggerEventAndIsActive("USER_REGISTRATION_OTP", true).size() > 0;
            if (hasActiveWorkflow) {
                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("email", customer.getEmail());
                payload.put("fullName", customer.getFullName());
                payload.put("otpCode", otp);
                workflowEngineService.triggerEvent("USER_REGISTRATION_OTP", payload);
            } else {
                Map<String, Object> ctx = new java.util.HashMap<>();
                ctx.put("otpCode", otp);
                ctx.put("fullName", customer.getFullName());
                eventPublisher.publishEvent(new com.kawai.events.SystemEmailEvent(this, customer.getEmail(), "Xác nhận đăng ký", "registration-otp", ctx));
            }
        }

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
            account.setIsActive(true); // <--- Kích hoạt tài khoản
            accountRepository.save(account);
            writeAuditLog(account, "VERIFY_OTP_SUCCESS", "Accounts", account.getId(), null,
                    "OTP verified successfully");
            return true;
        } else {
            writeAuditLog(account, "VERIFY_OTP_FAIL", "Accounts", account.getId(), null, "OTP verification failed");
            return false;
        }
    }

    @Override
    @Transactional
    public String requestPasswordReset(String email) {
        Account account = null;
        String username = "";

        Optional<Customer> optCustomer = customerRepository.findByEmail(email);
        if (optCustomer.isPresent()) {
            Customer customer = optCustomer.get();
            account = customer.getAccount();
            username = customer.getFullName() != null ? customer.getFullName() : customer.getAccount().getUsername();
        } else {
            Optional<Employee> optEmployee = employeeRepository.findByEmail(email);
            if (optEmployee.isPresent()) {
                Employee employee = optEmployee.get();
                account = employee.getAccount();
                username = employee.getFullName();
            }
        }

        if (account == null) {
            throw new IllegalArgumentException("Email không tồn tại");
        }

        String otp = String.format("%06d", 100000 + random.nextInt(900000));
        account.setResetPasswordToken(otp);
        account.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(15));
        accountRepository.save(account);

        writeAuditLog(account, "RESET_PASSWORD_REQUEST", "Accounts", account.getId(), null, "Reset OTP: " + otp);

        // Gửi email chứa OTP đặt lại mật khẩu qua Workflow Engine hoặc fallback
        boolean hasActiveWorkflow = workflowRepository.findByTriggerEventAndIsActive("USER_PASSWORD_RESET", true).size() > 0;
        if (hasActiveWorkflow) {
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("email", email);
            payload.put("fullName", username);
            payload.put("otpCode", otp);
            workflowEngineService.triggerEvent("USER_PASSWORD_RESET", payload);
        } else {
            Map<String, Object> ctx = new java.util.HashMap<>();
            ctx.put("resetLink", otp);
            ctx.put("fullName", username);
            eventPublisher.publishEvent(new com.kawai.events.SystemEmailEvent(this, email, "Đặt lại mật khẩu", "password-reset", ctx));
        }

        return otp;
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Account account = accountRepository.findAll().stream()
                .filter(a -> token.equals(a.getResetPasswordToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Token reset hết hạn hoặc không hợp lệ"));

        if (account.getResetPasswordExpiry() == null
                || account.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
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

        writeAuditLog(account, "RESET_PASSWORD_SUCCESS", "Accounts", account.getId(), oldHash,
                "Password reset successfully");
        return true;
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private void writeAuditLog(Account account, String action, String tableName, Long recordId, String oldValue,
            String newValue) {
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
