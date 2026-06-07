package com.kawai.controllers.web;

import com.kawai.models.core.Account;
import com.kawai.models.core.Customer;
import com.kawai.models.core.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.CustomerRepository;
import com.kawai.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String fullName,
                           @RequestParam(required = false, defaultValue = "Other") String gender,
                           @RequestParam(required = false, defaultValue = "N/A") String phone,
                           RedirectAttributes redirectAttributes) {
        
        if (accountRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
            return "redirect:/booking";
        }

        if (customerRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/booking";
        }

        // Assign default role (CUSTOMER)
        Role customerRole = roleRepository.findByRoleName("CUSTOMER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleName("CUSTOMER");
            return roleRepository.save(newRole);
        });

        // Create Account
        Account account = new Account();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(customerRole);
        account = accountRepository.save(account);

        // Create Customer
        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setGender(gender);
        customer.setPhone(phone);
        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/booking";
    }
}
