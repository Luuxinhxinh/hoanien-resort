package com.hoanien.config;

import com.hoanien.models.Account;
import com.hoanien.models.AuditLog;
import com.hoanien.repositories.AccountRepository;
import com.hoanien.repositories.AuditLogRepository;
import com.hoanien.utils.LogActivity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;

    @Around("@annotation(com.hoanien.utils.LogActivity)")
    public Object logActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("========== AUDIT LOG ASPECT TRIGGERED! ==========");
        // Execute the actual method
        Object result = joinPoint.proceed();

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogActivity logActivity = method.getAnnotation(LogActivity.class);

            String action = logActivity.action();
            String module = logActivity.module();

            // Get IP
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ipAddress = "0.0.0.0";
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
            }

            // Get User
            Account account = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String username = "";
                Object principal = auth.getPrincipal();
                if (principal instanceof UserDetails) {
                    username = ((UserDetails) principal).getUsername();
                } else {
                    username = principal.toString();
                }
                
                // Fetch account from DB
                account = accountRepository.findByUsername(username).orElse(null);
            }

            // Save Log
            AuditLog auditLog = new AuditLog();
            auditLog.setAccount(account);
            auditLog.setAction(action);
            
            // Try to dynamically extract table name and record ID
            Long recordId = 0L;
            try {
                String[] parameterNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();
                for (int i = 0; i < parameterNames.length; i++) {
                    if ("entityType".equals(parameterNames[i]) && args[i] instanceof String) {
                        module = (String) args[i]; // override module with actual table name
                    }
                    if ("id".equals(parameterNames[i]) && args[i] instanceof String) {
                        // Extract number from id (e.g., C-101 -> 101)
                        recordId = Long.parseLong(args[i].toString().replaceAll("[^0-9]", ""));
                    }
                }
            } catch (Exception ignored) { }

            // If it's a create method, we try to extract from the return value
            if (recordId == 0L && result instanceof org.springframework.http.ResponseEntity) {
                try {
                    org.springframework.http.ResponseEntity<?> response = (org.springframework.http.ResponseEntity<?>) result;
                    Object body = response.getBody();
                    if (body instanceof java.util.Map) {
                        Object data = ((java.util.Map<?, ?>) body).get("data");
                        if (data instanceof java.util.Map) {
                            Object createdId = ((java.util.Map<?, ?>) data).get("id");
                            if (createdId != null) {
                                recordId = Long.parseLong(createdId.toString().replaceAll("[^0-9]", ""));
                            }
                        }
                    }
                } catch (Exception ignored) { }
            }

            auditLog.setTableName(module);
            auditLog.setRecordId(recordId); // Dynamic extraction
            auditLog.setIpAddress(ipAddress);
            auditLog.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }

        return result;
    }
}
