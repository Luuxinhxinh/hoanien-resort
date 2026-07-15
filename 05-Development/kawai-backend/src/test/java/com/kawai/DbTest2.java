package com.kawai;

import com.kawai.models.Account;
import com.kawai.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DbTest2 {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void testDb() {
        List<Account> accounts = accountRepository.findAll();
        for (Account a : accounts) {
            System.out.println(a.getUsername() + " -> " + a.getPasswordHash() + " (Active: " + a.getIsActive() + ")");
        }
        
        System.out.println("FIND ADMIN: " + accountRepository.findByUsername("admin").isPresent());
    }
}
