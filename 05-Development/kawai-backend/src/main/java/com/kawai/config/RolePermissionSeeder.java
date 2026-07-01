package com.kawai.config;

import com.kawai.models.Role;
import com.kawai.repositories.RoleRepository;
import com.kawai.security.RolePermissionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Chạy một lần khi ứng dụng khởi động.
 * Tự động gán quyền hạn mặc định cho các Role trong DB
 * nếu Role chưa có permissions (null hoặc rỗng).
 *
 * Nếu Admin đã cấu hình tay qua UI → giá trị đó được giữ nguyên (không ghi đè).
 */
@Component
public class RolePermissionSeeder implements ApplicationRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Role> roles = roleRepository.findAll();
        int patched = 0;

        for (Role role : roles) {
            String defaults = RolePermissionConstants.getDefaultPermissionsFor(role.getRoleName());
            if (defaults != null && !defaults.isEmpty()) {
                String current = role.getPermissions() == null ? "" : role.getPermissions().trim();
                java.util.Set<String> perms = new java.util.HashSet<>(java.util.Arrays.asList(current.split(",")));
                perms.remove(""); // Clean up empty string if any
                java.util.Set<String> defaultPerms = new java.util.HashSet<>(java.util.Arrays.asList(defaults.split(",")));
                
                if (!perms.containsAll(defaultPerms)) {
                    perms.addAll(defaultPerms);
                    role.setPermissions(String.join(",", perms));
                    roleRepository.save(role);
                    patched++;
                    System.out.printf("[RBAC] Role '%s' → merged full default permissions.%n", role.getRoleName());
                } else {
                    System.out.printf("[RBAC] Role '%s' → already has full default permissions, skipped.%n", role.getRoleName());
                }
            }
        }

        if (patched > 0) {
            System.out.printf("[RBAC] ✅ Seeded full default permissions for %d role(s).%n", patched);
        } else {
            System.out.println("[RBAC] ✅ All roles already have their full default permissions configured.");
        }
    }
}
