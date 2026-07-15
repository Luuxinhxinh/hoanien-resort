package com.kawai;

import com.kawai.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DbTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void testDb() {
        System.out.println("ACCOUNTS COUNT: " + accountRepository.count());
    }
}
