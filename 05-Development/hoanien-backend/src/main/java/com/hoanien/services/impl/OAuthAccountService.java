package com.hoanien.services.impl;

import com.hoanien.models.Account;
import com.hoanien.models.Customer;
import com.hoanien.models.Role;
import com.hoanien.repositories.AccountRepository;
import com.hoanien.repositories.CustomerRepository;
import com.hoanien.repositories.RoleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthAccountService {

    private static final Logger log = LoggerFactory.getLogger(OAuthAccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private com.hoanien.repositories.MembershipTierRepository membershipTierRepository;

    @Autowired
    private com.hoanien.repositories.EmployeeRepository employeeRepository;

    // Không inject PasswordEncoder để tránh circular dependency

    /**
     * Tìm hoặc tạo Account + Customer cho người dùng đăng nhập qua Google OAuth.
     * Phương thức này chạy trong một transaction riêng biệt, đảm bảo data được
     * commit.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Account findOrCreateOAuthAccount(String email, String fullName) {
        log.info(">>> findOrCreateOAuthAccount called with email={}", email);
        if (email == null || email.isBlank()) {
            log.warn("OAuth login bị từ chối: email là null hoặc rỗng");
            return null;
        }

        // 1. Tìm Customer đã tồn tại theo email
        Customer existingCustomer = customerRepository.findByEmail(email).orElse(null);
        if (existingCustomer != null && existingCustomer.getAccount() != null) {
            log.info("OAuth login: tìm thấy Customer đã tồn tại theo email={}, gắn kết Account tương ứng (ID={})", email, existingCustomer.getAccount().getId());
            return existingCustomer.getAccount();
        }

        // 2. Tìm Employee đã tồn tại theo email
        com.hoanien.models.Employee existingEmployee = employeeRepository.findByEmail(email).orElse(null);
        if (existingEmployee != null && existingEmployee.getAccount() != null) {
            log.info("OAuth login: tìm thấy Employee đã tồn tại theo email={}, gắn kết Account tương ứng (ID={})", email, existingEmployee.getAccount().getId());
            return existingEmployee.getAccount();
        }

        // 3. Fallback: Tìm Account theo username
        Account existing = accountRepository.findByUsername(email).orElse(null);
        if (existing != null) {
            log.info("OAuth login: tìm thấy Account theo username={}, trả về Account ID={}", email, existing.getId());
            // Nếu Account tồn tại nhưng chưa có Customer record liên kết, tự tạo Customer bổ sung
            if (customerRepository.findByAccount_Username(email).isEmpty() && employeeRepository.findByAccountUsername(email).isEmpty()) {
                Customer newCust = new Customer();
                newCust.setAccount(existing);
                newCust.setFullName(fullName != null && !fullName.isBlank() ? fullName : "Khách hàng Google");
                newCust.setEmail(email);
                newCust.setGender("Other");
                newCust.setPhone("");
                newCust.setMembershipTier(membershipTierRepository.findByTierNameIgnoreCase("Regular").orElse(null));
                customerRepository.save(newCust);
                log.info("Bổ sung Customer record mới liên kết với Account ID={}", existing.getId());
            }
            return existing;
        }

        log.info("OAuth login: Không tìm thấy tài khoản cũ. Tiến hành tạo Account + Customer mới cho email={}", email);

        // Lấy role CUSTOMER NORMAL (role_id = 9 trong data.sql)
        Role customerRole = roleRepository.findByRoleName("CUSTOMER NORMAL")
                .or(() -> roleRepository.findByRoleName("CUSTOMER"))
                .orElseGet(() -> {
                    log.warn("Role 'CUSTOMER NORMAL' không tồn tại, đang tạo role mới...");
                    Role newRole = new Role();
                    newRole.setRoleName("CUSTOMER NORMAL");
                    return roleRepository.save(newRole);
                });

        // Tạo Account mới
        Account account = new Account();
        account.setUsername(email);
        account.setPasswordHash(new BCryptPasswordEncoder().encode("OAUTH2_GOOGLE_PLACEHOLDER"));
        account.setRole(customerRole);
        account.setIsActive(true);
        account = accountRepository.save(account);
        log.info("Đã tạo Account mới: id={}, username={}", account.getId(), account.getUsername());

        // Tạo Customer liên kết đầy đủ thông tin sẵn có từ Google
        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(fullName != null && !fullName.isBlank() ? fullName : "Khách hàng Google");
        customer.setEmail(email);
        customer.setGender("Other");
        customer.setPhone("");
        customer.setCccdPassportEncrypted(null);
        customer.setMembershipTier(membershipTierRepository.findByTierNameIgnoreCase("Regular").orElse(null));
        customerRepository.save(customer);
        log.info("Đã tạo Customer mới: id={}, email={}, fullName={}", customer.getId(), email, customer.getFullName());

        return account;
    }
}