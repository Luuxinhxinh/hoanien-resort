package com.kawai.security;

import com.kawai.models.Account;
import com.kawai.repositories.AccountRepository;
import com.kawai.models.Workflow;
import com.kawai.repositories.WorkflowRepository;
import com.kawai.models.AuditLog;
import com.kawai.repositories.AuditLogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthenticationEvents {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private void writeAuditLog(Account account, String action, String tableName, Long recordId, String oldValues, String newValues) {
        try {
            AuditLog log = new AuditLog();
            log.setAccount(account);
            log.setAction(action);
            log.setTableName(tableName);
            log.setRecordId(recordId);
            log.setOldValue(oldValues);
            log.setNewValue(newValues);
            log.setIpAddress("SYSTEM");
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            // ignore
        }
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent success) {
        Object principal = success.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            Optional<Account> optAcc = accountRepository.findByUsername(username);
            if (optAcc.isPresent()) {
                Account account = optAcc.get();
                if (account.getFailedLoginAttempts() != null && account.getFailedLoginAttempts() > 0) {
                    account.setFailedLoginAttempts(0);
                    account.setLockoutTime(null);
                    accountRepository.save(account);
                }
            }
        }
    }

    @EventListener
    @Transactional
    public void onFailure(AuthenticationFailureBadCredentialsEvent failures) {
        String username = (String) failures.getAuthentication().getPrincipal();
        Optional<Account> optAcc = accountRepository.findByUsername(username);
        
        if (optAcc.isPresent()) {
            Account account = optAcc.get();
            
            int maxAttempts = 5;
            int lockMinutes = 15;

            try {
                Optional<Workflow> activeWfOpt = workflowRepository.findByTriggerEventAndIsActive("ACCOUNT_SECURITY", true).stream().findFirst();
                if (activeWfOpt.isPresent()) {
                    Workflow wf = activeWfOpt.get();
                    if (wf.getConditionsJson() != null && !wf.getConditionsJson().trim().isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> conds = mapper.readValue(wf.getConditionsJson(), new TypeReference<Map<String, Object>>() {});
                        if (conds.containsKey("failed_login_attempts")) {
                            maxAttempts = Integer.parseInt(conds.get("failed_login_attempts").toString());
                        }
                        if (conds.containsKey("lockout_time_minutes")) {
                            lockMinutes = Integer.parseInt(conds.get("lockout_time_minutes").toString());
                        }
                    }
                }
            } catch (Exception e) {}

            int attempts = account.getFailedLoginAttempts() != null ? account.getFailedLoginAttempts() : 0;
            attempts++;
            account.setFailedLoginAttempts(attempts);

            if (attempts >= maxAttempts) {
                account.setLockoutTime(LocalDateTime.now().plusMinutes(lockMinutes));
                account.setIsActive(false); // Khóa tài khoản
                
                // Ghi log báo khóa
                writeAuditLog(account, "ACCOUNT_LOCKED", "Accounts", account.getId(), null, "Tài khoản bị khóa do nhập sai mật khẩu " + attempts + " lần.");
            } else {
                writeAuditLog(account, "LOGIN_FAIL", "Accounts", account.getId(), null, "Nhập sai mật khẩu lần thứ " + attempts);
            }

            accountRepository.save(account);
        }
    }
}
