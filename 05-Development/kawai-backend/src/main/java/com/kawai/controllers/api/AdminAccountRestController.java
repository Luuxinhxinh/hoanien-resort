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
    private final com.kawai.repositories.EmployeeRepository employeeRepository;

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

    @PostMapping("/customers/{id}/anonymize")
    public ResponseEntity<?> anonymizeCustomer(@PathVariable Long id) {
        System.out.println("========== ANONYMIZE CUSTOMER API HIT! ID: " + id + " ==========");
        try {
            userService.anonymizeCustomer(id);
            return ResponseEntity.ok(Map.of("message", "Dữ liệu khách hàng đã được ẩn danh thành công. Lịch sử hóa đơn vẫn được lưu trữ."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("========== ERROR IN ANONYMIZE API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
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
                dto.setGender(payload.get("gender") != null ? payload.get("gender") : "Nam");
                String salaryStr = payload.get("salary");
                dto.setSalary(salaryStr != null && !salaryStr.isEmpty() ? new BigDecimal(salaryStr) : BigDecimal.ZERO);
                
                Employee emp = userService.createEmployeeAccount(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(emp);
            } else {
                dto.setUsername(payload.get("username") != null && !payload.get("username").isEmpty() ? payload.get("username") : payload.get("email").split("@")[0]);
                dto.setPassword(payload.get("password") != null && !payload.get("password").isEmpty() ? payload.get("password") : "123456");
                dto.setPhone(payload.get("phone"));
                dto.setCccd(payload.get("cccd"));
                dto.setGender(payload.get("gender") != null ? payload.get("gender") : "Nam");
                
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
                Map<String, String> updatePayload = Map.of("status", String.valueOf(newStatus));
                userService.updateEmployeeAccount(empId, updatePayload);
            } else if (id.startsWith("C-")) {
                Long cusId = Long.parseLong(id.substring(2));
                Map<String, String> updatePayload = Map.of("status", String.valueOf(newStatus));
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

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable String id) {
        System.out.println("========== DELETE ACCOUNT API HIT! ID: " + id + " ==========");
        try {
            // Soft delete by disabling the account
            if (id.startsWith("E-")) {
                Long empId = Long.parseLong(id.substring(2));
                userService.updateEmployeeAccount(empId, Map.of("status", "false"));
            } else if (id.startsWith("C-")) {
                Long cusId = Long.parseLong(id.substring(2));
                userService.updateCustomerAccount(cusId, Map.of("status", "false"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID format"));
            }
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            System.out.println("========== ERROR IN DELETE API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/accounts/{id}/permissions")
    public ResponseEntity<?> getAccountPermissions(@PathVariable String id) {
        try {
            if (id.startsWith("E-")) {
                Long empId = Long.parseLong(id.substring(2));
                com.kawai.models.Employee emp = employeeRepository.findById(empId)
                        .orElseThrow(() -> new RuntimeException("Employee not found"));
                com.kawai.models.Account account = emp.getAccount();
                if (account == null || account.getRole() == null) {
                    return ResponseEntity.ok(java.util.Collections.emptyList());
                }
                String perms = account.getRole().getPermissions();
                if (perms == null || perms.isEmpty()) {
                    return ResponseEntity.ok(java.util.Collections.emptyList());
                }
                return ResponseEntity.ok(java.util.Arrays.asList(perms.split(",")));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID format"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/accounts/{id}/permissions")
    public ResponseEntity<?> resetAccountPermissions(@PathVariable String id) {
        try {
            if (id.startsWith("E-")) {
                Long empId = Long.parseLong(id.substring(2));
                com.kawai.models.Employee emp = employeeRepository.findById(empId)
                        .orElseThrow(() -> new RuntimeException("Employee not found"));
                com.kawai.models.Account account = emp.getAccount();
                if (account == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Employee has no account"));
                }

                com.kawai.models.Role currentRole = account.getRole();
                if (currentRole == null || !currentRole.getRoleName().startsWith("CUSTOM_ROLE_")) {
                    return ResponseEntity.ok(Map.of("message", "No custom permissions to reset"));
                }

                // Lấy base role name từ description
                String desc = currentRole.getDescription();
                String baseRoleName = null;
                if (desc != null && desc.contains("Base: ")) {
                    baseRoleName = desc.substring(desc.indexOf("Base: ") + 6).replace(")", "").trim();
                }

                if (baseRoleName == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Cannot determine base role"));
                }

                final String finalBaseRoleName = baseRoleName;
                com.kawai.models.Role baseRole = userService.findRoleByName(finalBaseRoleName)
                        .orElseThrow(() -> new RuntimeException("Base role not found: " + finalBaseRoleName));

                // Gán lại role gốc cho account
                account.setRole(baseRole);
                userService.saveAccount(account);

                // Xóa CUSTOM_ROLE khỏi DB
                userService.deleteRole(currentRole.getId());

                return ResponseEntity.ok(Map.of("message", "Permissions reset to default successfully"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID format"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/accounts/{id}/permissions")
    public ResponseEntity<?> updateAccountPermissions(@PathVariable String id, @RequestBody java.util.List<String> permissions) {
        try {
            if (id.startsWith("E-")) {
                Long empId = Long.parseLong(id.substring(2));
                com.kawai.models.Employee emp = employeeRepository.findById(empId)
                        .orElseThrow(() -> new RuntimeException("Employee not found"));
                com.kawai.models.Account account = emp.getAccount();
                if (account == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Employee has no account"));
                }
                
                // Chuẩn hóa quyền, loại bỏ OP_ nếu Frontend gửi thừa
                java.util.List<String> cleanedPerms = permissions.stream()
                        .map(p -> p.startsWith("OP_") ? p.substring(3) : p)
                        .collect(java.util.stream.Collectors.toList());

                userService.updateCustomPermissions(account.getId(), cleanedPerms);
                return ResponseEntity.ok(Map.of("message", "Permissions updated successfully"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID format"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
