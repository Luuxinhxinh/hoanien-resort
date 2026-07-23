package com.kawai.guesttests;

import com.kawai.controllers.web.ProfileController;
import com.kawai.models.Customer;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.utils.EncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestServicesIntegrationTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProfileController profileController;

    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(1L);
        mockCustomer.setFullName("Nguyen Guest");
        mockCustomer.setEmail("guest@kawai.com");
        mockCustomer.setPhone("0987654321");
        mockCustomer.setGender("Male");
        mockCustomer.setCccdPassportEncrypted(null);
    }

    @Test
    void testEncryptionUtils_EncryptDecrypt() {
        String originalValue = "012345678912";
        String encrypted = EncryptionUtils.encrypt(originalValue);

        assertNotNull(encrypted);
        assertNotEquals(originalValue, encrypted);

        String decrypted = EncryptionUtils.decrypt(encrypted);
        assertEquals(originalValue, decrypted);
    }

    @Test
    void testEditProfile_WithNewCccd_ShouldEncrypt() {
        when(authentication.getName()).thenReturn("guest_user");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(customerRepository.findByAccount_Username("guest_user")).thenReturn(Optional.of(mockCustomer));

        String result = profileController.editProfile(
                authentication,
                "Updated Name",
                "guest@kawai.com",
                "Female",
                "0911223344",
                "123456789012",
                "1990-01-01",
                redirectAttributes);

        assertEquals("redirect:/profile", result);
        assertEquals("Updated Name", mockCustomer.getFullName());
        assertEquals("Female", mockCustomer.getGender());
        assertEquals("0911223344", mockCustomer.getPhone());
        assertNotNull(mockCustomer.getCccdPassportEncrypted());
        assertNotEquals("123456789012", mockCustomer.getCccdPassportEncrypted());
        assertEquals("123456789012", EncryptionUtils.decrypt(mockCustomer.getCccdPassportEncrypted()));

        verify(customerRepository, times(1)).save(mockCustomer);
    }

    @Test
    void testEditProfile_WithPlaceholderCccd_ShouldNotEncryptOrChange() {
        mockCustomer.setCccdPassportEncrypted(EncryptionUtils.encrypt("123456789012"));
        when(authentication.getName()).thenReturn("guest_user");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(customerRepository.findByAccount_Username("guest_user")).thenReturn(Optional.of(mockCustomer));

        String result = profileController.editProfile(
                authentication,
                "Updated Name",
                "guest@kawai.com",
                "Female",
                "0911223344",
                "********",
                "1990-01-01",
                redirectAttributes);

        assertEquals("redirect:/profile", result);
        assertEquals("123456789012", EncryptionUtils.decrypt(mockCustomer.getCccdPassportEncrypted()));
        verify(customerRepository, times(1)).save(mockCustomer);
    }
}
