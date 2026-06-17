package com.kawai.services.impl;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoleRepository;

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

        // Nếu đã có account rồi thì trả về luôn
        Account existing = accountRepository.findByUsername(email).orElse(null);
        if (existing != null) {
            log.info("OAuth login: tìm thấy account đã tồn tại cho email={}", email);
            return existing;
        }

        log.info("OAuth login: tạo account mới cho email={}", email);

        // Lấy role CUSTOMER NORMAL (role_id = 9 trong data.sql)
        Role customerRole = roleRepository.findByRoleName("CUSTOMER NORMAL")
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

        // Tạo Customer liên kết
        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(fullName != null ? fullName : "Khách hàng Google");
        customer.setEmail(email);
        customer.setGender("Other");
        customer.setPhone("G-" + System.currentTimeMillis());
        customerRepository.save(customer);
        log.info("Đã tạo Customer mới: id={}, email={}", customer.getId(), email);

        return account;
    }
}