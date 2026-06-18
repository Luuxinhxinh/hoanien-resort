package com.kawai.config;

import com.kawai.models.Account;
import com.kawai.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminPasswordResetRunner implements CommandLineRunner {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Optional<Account> adminOpt = accountRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            Account admin = adminOpt.get();
            // Cưỡng chế reset mật khẩu thành admin123
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setIsActive(true);
            admin.setFailedLoginAttempts(0);
            admin.setLockoutTime(null);
            accountRepository.save(admin);
            System.out.println("=================================================");
            System.out.println("✅ ADMIN PASSWORD FORCED RESET TO: admin123");
            System.out.println("=================================================");
        }
    }
}
