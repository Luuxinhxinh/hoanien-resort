package com.kawai.config;

import com.kawai.models.Account;
import com.kawai.models.AuditLog;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.AuditLogRepository;
import com.kawai.utils.LogActivity;
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

    @Around("@annotation(com.kawai.utils.LogActivity)")
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
            auditLog.setTableName(module);
            auditLog.setRecordId(0L); // Optional for general activities
            auditLog.setIpAddress(ipAddress);
            auditLog.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }

        return result;
    }
}
