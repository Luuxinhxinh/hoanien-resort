package com.kawai.controllers.api;

import com.kawai.dto.CreateEmployeeDTO;
import com.kawai.models.Employee;
import com.kawai.models.Customer;
import com.kawai.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kawai.models.Role;
import com.kawai.repositories.RoleRepository;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/api/v1")
@RequiredArgsConstructor
public class AdminAccountRestController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @PostMapping("/employees")
    public ResponseEntity<Employee> createEmployeeAccount(@RequestBody CreateEmployeeDTO dto) {
        System.out.println("========== CREATE EMPLOYEE API HIT! ==========");
        System.out.println("Payload: " + dto);
        try {
            Employee createdEmployee = userService.createEmployeeAccount(dto);
            System.out.println("========== SUCCESS! Employee created with ID: " + createdEmployee.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (Exception e) {
            System.out.println("========== ERROR IN API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomerAccount(@RequestBody CreateEmployeeDTO dto) {
        System.out.println("========== CREATE CUSTOMER API HIT! ==========");
        System.out.println("Payload: " + dto);
        try {
            Customer createdCustomer = userService.createCustomerAccount(dto);
            System.out.println("========== SUCCESS! Customer created with ID: " + createdCustomer.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
        } catch (Exception e) {
            System.out.println("========== ERROR IN API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, String> payload) {
        System.out.println("========== CREATE ACCOUNT API HIT! ==========");
        System.out.println("Payload: " + payload);
        try {
            String type = payload.get("type");
            String roleName = payload.get("role");
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

            CreateEmployeeDTO dto = new CreateEmployeeDTO();
            dto.setFullName(payload.get("name"));
            dto.setEmail(payload.get("email"));
            dto.setRoleId(role.getId());

            if ("Nhân viên".equals(type)) {
                dto.setUsername(payload.get("username"));
                dto.setPassword(payload.get("password"));
                dto.setPhone(payload.get("phone"));
                dto.setCccd(payload.get("cccd"));
                dto.setGender(payload.get("gender"));
                String salaryStr = payload.get("salary");
                dto.setSalary(salaryStr != null && !salaryStr.isEmpty() ? new BigDecimal(salaryStr) : BigDecimal.ZERO);
                
                Employee emp = userService.createEmployeeAccount(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(emp);
            } else {
                dto.setUsername(payload.get("email").split("@")[0]);
                dto.setPassword("123");
                dto.setPhone("");
                dto.setGender("MALE");
                
                Customer cus = userService.createCustomerAccount(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(cus);
            }
        } catch (Exception e) {
            System.out.println("========== ERROR IN API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable String id, @RequestBody Map<String, String> payload) {
        System.out.println("========== UPDATE ACCOUNT API HIT! ID: " + id + " ==========");
        System.out.println("Payload: " + payload);
        try {
            if (id.startsWith("E-")) {
                Long empId = Long.parseLong(id.substring(2));
                Employee emp = userService.updateEmployeeAccount(empId, payload);
                return ResponseEntity.ok(emp);
            } else if (id.startsWith("C-")) {
                Long cusId = Long.parseLong(id.substring(2));
                Customer cus = userService.updateCustomerAccount(cusId, payload);
                return ResponseEntity.ok(cus);
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID format"));
        } catch (Exception e) {
            System.out.println("========== ERROR IN API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/accounts/{id}/toggle")
    public ResponseEntity<?> toggleAccountStatus(@PathVariable String id, @RequestBody Map<String, Boolean> payload) {
        System.out.println("========== TOGGLE ACCOUNT API HIT! ID: " + id + " ==========");
        try {
            Boolean newStatus = payload.get("status");
            if (id.startsWith("E-")) {
                Long empId = Long.parseLong(id.substring(2));
                // We need to fetch and save, but for brevity, maybe userService has a method?
                // Let's get the account directly from the employee.
                // Assuming we can just do a partial update.
                Map<String, String> updatePayload = Map.of("isActive", String.valueOf(newStatus));
                userService.updateEmployeeAccount(empId, updatePayload);
            } else if (id.startsWith("C-")) {
                Long cusId = Long.parseLong(id.substring(2));
                Map<String, String> updatePayload = Map.of("isActive", String.valueOf(newStatus));
                userService.updateCustomerAccount(cusId, updatePayload);
            } else {
                 return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID format"));
            }
            return ResponseEntity.ok(Map.of("message", "Toggled successfully"));
        } catch (Exception e) {
            System.out.println("========== ERROR IN API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
