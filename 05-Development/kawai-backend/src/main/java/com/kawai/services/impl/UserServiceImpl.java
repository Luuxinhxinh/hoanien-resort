package com.kawai.services.impl;

import com.kawai.dto.CreateEmployeeDTO;
import com.kawai.models.Account;
import com.kawai.models.Employee;
import com.kawai.models.Role;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.RoleRepository;
import com.kawai.services.interfaces.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kawai.repositories.CustomerRepository;
import com.kawai.models.Customer;

@Service
public class UserServiceImpl implements UserService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(AccountRepository accountRepository, 
                           EmployeeRepository employeeRepository, 
                           CustomerRepository customerRepository,
                           RoleRepository roleRepository, 
                           PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Employee createEmployeeAccount(CreateEmployeeDTO dto) {
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (employeeRepository.existsByCccd(dto.getCccd())) {
            throw new IllegalArgumentException("CCCD already exists");
        }
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        account.setRole(role);
        account.setIsActive(true);
        account = accountRepository.save(account);

        Employee employee = new Employee();
        employee.setAccount(account);
        employee.setFullName(dto.getFullName());
        employee.setGender(dto.getGender());
        if (dto.getPhone() != null && !com.kawai.utils.ValidationUtils.isValidPhone(dto.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (dto.getCccd() != null && !com.kawai.utils.ValidationUtils.isValidDocument(dto.getCccd())) {
            throw new IllegalArgumentException("Invalid CCCD format");
        }
        employee.setCccd(dto.getCccd());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setSalary(dto.getSalary());
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Customer createCustomerAccount(CreateEmployeeDTO dto) {
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        account.setRole(role);
        account.setIsActive(true);
        account = accountRepository.save(account);

        Customer customer = new Customer();
        customer.setAccount(account);
        customer.setFullName(dto.getFullName());
        customer.setGender(dto.getGender());
        if (dto.getPhone() != null && !com.kawai.utils.ValidationUtils.isValidPhone(dto.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        customer.setPhone(dto.getPhone());
        customer.setEmail(dto.getEmail());
        if (dto.getCccd() != null) {
            customer.setCccdPassportEncrypted(dto.getCccd());
        }
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Employee updateEmployeeAccount(Long id, java.util.Map<String, String> payload) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (payload.containsKey("name")) employee.setFullName(payload.get("name"));
        if (payload.containsKey("email")) employee.setEmail(payload.get("email"));
        if (payload.containsKey("phone")) {
            String phone = payload.get("phone");
            if (!com.kawai.utils.ValidationUtils.isValidPhone(phone)) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            employee.setPhone(phone);
        }
        if (payload.containsKey("cccd")) {
            String cccd = payload.get("cccd");
            if (!com.kawai.utils.ValidationUtils.isValidDocument(cccd)) {
                throw new IllegalArgumentException("Invalid CCCD format");
            }
            employee.setCccd(cccd);
        }
        if (payload.containsKey("gender")) employee.setGender(payload.get("gender"));
        if (payload.containsKey("salary") && !payload.get("salary").isEmpty()) {
            employee.setSalary(new java.math.BigDecimal(payload.get("salary")));
        }

        Account account = employee.getAccount();
        if (account != null) {
            if (payload.containsKey("role")) {
                Role role = roleRepository.findByRoleName(payload.get("role"))
                        .orElseThrow(() -> new IllegalArgumentException("Role not found"));
                
                // Prevent changing role of Admin
                if ("Admin".equalsIgnoreCase(account.getRole().getRoleName()) && !"Admin".equalsIgnoreCase(role.getRoleName())) {
                    throw new IllegalArgumentException("Không thể thay đổi vai trò của tài khoản Admin");
                }
                account.setRole(role);
            }
            if (payload.containsKey("status")) {
                boolean newStatus = Boolean.parseBoolean(payload.get("status"));
                if (!newStatus && "Admin".equalsIgnoreCase(account.getRole().getRoleName())) {
                    throw new IllegalArgumentException("Không thể vô hiệu hóa tài khoản Admin");
                }
                account.setIsActive(newStatus);
            }
            // Optional: Handle password update if provided
            if (payload.containsKey("password") && !payload.get("password").trim().isEmpty()) {
                account.setPasswordHash(passwordEncoder.encode(payload.get("password")));
            }
            accountRepository.save(account);
        }

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Customer updateCustomerAccount(Long id, java.util.Map<String, String> payload) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (payload.containsKey("name")) customer.setFullName(payload.get("name"));
        if (payload.containsKey("email")) customer.setEmail(payload.get("email"));
        if (payload.containsKey("phone")) {
            String phone = payload.get("phone");
            if (!com.kawai.utils.ValidationUtils.isValidPhone(phone)) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            customer.setPhone(phone);
        }
        if (payload.containsKey("gender")) customer.setGender(payload.get("gender"));
        if (payload.containsKey("cccd")) customer.setCccdPassportEncrypted(payload.get("cccd"));

        Account account = customer.getAccount();
        if (account != null) {
            if (payload.containsKey("role")) {
                Role role = roleRepository.findByRoleName(payload.get("role"))
                        .orElseThrow(() -> new IllegalArgumentException("Role not found"));
                account.setRole(role);
            }
            if (payload.containsKey("status")) {
                account.setIsActive(Boolean.parseBoolean(payload.get("status")));
            }
            // Optional: Handle password update if provided
            if (payload.containsKey("password") && !payload.get("password").trim().isEmpty()) {
                account.setPasswordHash(passwordEncoder.encode(payload.get("password")));
            }
            accountRepository.save(account);
        }

        return customerRepository.save(customer);
    }
}
