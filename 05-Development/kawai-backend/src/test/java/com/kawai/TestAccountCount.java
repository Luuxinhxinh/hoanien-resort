package com.kawai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.kawai.repositories.AccountRepository;
import com.kawai.models.Account;
import java.util.List;

@SpringBootTest
public class TestAccountCount {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void testCount() {
        System.out.println("===============================");
        List<Account> accounts = accountRepository.findAll();
        System.out.println("TOTAL ACCOUNTS IN DB: " + accounts.size());
        for (Account a : accounts) {
            System.out.println("Account: " + a.getId() + " - " + a.getUsername());
        }
        System.out.println("===============================");
    }
}
