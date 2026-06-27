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

    @Autowired
    private com.kawai.repositories.MembershipTierRepository membershipTierRepository;

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

        // Ưu tiên tìm Customer theo email vì email trong Customer là duy nhất.
        // Nếu user đã đăng ký tay (username khác email) rồi login bằng Google,
        // việc này sẽ trả về đúng tài khoản đó thay vì cố tạo mới và gây lỗi 500.
        Customer existingCustomer = customerRepository.findByEmail(email).orElse(null);
        if (existingCustomer != null) {
            log.info("OAuth login: tìm thấy Customer đã tồn tại theo email={}, trả về Account tương ứng", email);
            return existingCustomer.getAccount();
        }

        // Fallback: Tìm Account theo username
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
        customer.setPhone("N/A");
        customer.setCccdPassportEncrypted(null);
        customer.setMembershipTier(membershipTierRepository.findByTierNameIgnoreCase("Regular").orElse(null));
        customerRepository.save(customer);
        log.info("Đã tạo Customer mới: id={}, email={}", customer.getId(), email);

        return account;
    }
}