package com.kawai.services;

import com.kawai.services.impl.EncryptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UC03 - AES Encryption (EncryptionService)")
public class EncryptionServiceUC03Test {

    private EncryptionServiceImpl encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionServiceImpl();
        // Inject a 32-byte key for AES-256
        ReflectionTestUtils.setField(encryptionService, "secretKey", "ThisIsASecretKeyForAES256Encrypt"); 
    }

    @Test
    @DisplayName("TC-UC03-001 | Verify thuật toán mã hóa 2 chiều AES")
    void testEncryptDecrypt_Success() throws Exception {
        String plainText = "012345678912";
        
        String encrypted = encryptionService.encrypt(plainText);
        
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted); // Must not be plain text
        
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(plainText, decrypted); // Must decrypt back to original
    }
}
