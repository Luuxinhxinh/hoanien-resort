package com.kawai;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordTest {
    @Test
    void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash1 = "$2a$10$4bnThA4xQVw1rF2POQv78uAQll2KsUsgF32JaYiVG5d2d3Fhgk83q";
        String hash2 = "$2a$10$ikP3XeXnMx/oLodhs4wqBO61AhyuE4dWtSJDJcOH7D2ii5Vrgq0bG";
        String[] pwds = {"Admin@123", "123456aA@", "password123", "admin123", "123", "0000"};
        for(String p : pwds) {
             if(encoder.matches(p, hash1)) System.out.println("HASH 1 IS: " + p);
             if(encoder.matches(p, hash2)) System.out.println("HASH 2 IS: " + p);
        }
    }
}
