package com.kawai;

import com.kawai.models.Account;
import com.kawai.models.Customer;
import com.kawai.models.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthAndProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        if (roleRepository.findByRoleName("CUSTOMER").isEmpty()) {
            Role role = new Role();
            role.setRoleName("CUSTOMER");
            roleRepository.save(role);
        }
        if (roleRepository.findByRoleName("ADMIN").isEmpty()) {
            Role role = new Role();
            role.setRoleName("ADMIN");
            roleRepository.save(role);
        }
    }

    @Test
    public void testRegistrationAndHashing() throws Exception {
        mockMvc.perform(post("/auth/register")
                .param("username", "testuser")
                .param("password", "testpass")
                .param("email", "test@test.com")
                .param("fullName", "Test User")
                .param("gender", "Male")
                .param("phone", "0123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking"));

        Account account = accountRepository.findByUsername("testuser").orElse(null);
        assertNotNull(account);
        assertTrue(passwordEncoder.matches("testpass", account.getPasswordHash()));
        assertEquals("CUSTOMER", account.getRole().getRoleName());
        
        Customer customer = customerRepository.findByAccount_Username("testuser").orElse(null);
        assertNotNull(customer);
        assertEquals("test@test.com", customer.getEmail());
    }

    @Test
    public void testLoginAndLogout() throws Exception {
        Account account = new Account();
        account.setUsername("loginuser");
        account.setPasswordHash(passwordEncoder.encode("loginpass"));
        account.setRole(roleRepository.findByRoleName("CUSTOMER").get());
        accountRepository.save(account);

        // Test login
        mockMvc.perform(formLogin("/auth/login").user("loginuser").password("loginpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Test logout
        mockMvc.perform(logout("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testRoleBasedFilter() throws Exception {
        // Unauthenticated access
        mockMvc.perform(get("/templates/admin/dashboard"))
                .andExpect(status().is3xxRedirection()); // Redirects to login

        // Access with CUSTOMER role
        mockMvc.perform(get("/templates/admin/dashboard")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("guest").roles("CUSTOMER")))
                .andExpect(status().isForbidden());

        // Access with ADMIN role (will return 404 Not Found since endpoint doesn't exist, but it passed authorization)
        mockMvc.perform(get("/templates/admin/dashboard")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("templates/admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "profileuser")
    public void testProfileViewAndEdit() throws Exception {
        Account account = new Account();
        account.setUsername("profileuser");
        account.setPasswordHash("dummy");
        account.setRole(roleRepository.findByRoleName("CUSTOMER").get());
        accountRepository.save(account);

        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setEmail("old@test.com");
        customer.setFullName("Old Name");
        customer.setGender("Male");
        customer.setPhone("0123");
        customerRepository.save(customer);

        // View Profile
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("guest/profile"))
                .andExpect(model().attributeExists("customer"));

        // Edit Profile
        mockMvc.perform(post("/profile/edit")
                .param("fullName", "New Name")
                .param("email", "new@test.com")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        Customer updatedCustomer = customerRepository.findByAccount_Username("profileuser").get();
        assertEquals("New Name", updatedCustomer.getFullName());
        assertEquals("new@test.com", updatedCustomer.getEmail());
    }

    @Test
    @WithMockUser(username = "passuser")
    public void testChangePassword() throws Exception {
        Account account = new Account();
        account.setUsername("passuser");
        account.setPasswordHash(passwordEncoder.encode("oldpass"));
        account.setRole(roleRepository.findByRoleName("CUSTOMER").get());
        accountRepository.save(account);

        // Failed change
        mockMvc.perform(post("/profile/change-password")
                .param("oldPassword", "wrongpass")
                .param("newPassword", "newpass")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("error"));

        // Successful change
        mockMvc.perform(post("/profile/change-password")
                .param("oldPassword", "oldpass")
                .param("newPassword", "newpass")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("success"));

        Account updatedAccount = accountRepository.findByUsername("passuser").get();
        assertTrue(passwordEncoder.matches("newpass", updatedAccount.getPasswordHash()));
    }
}
