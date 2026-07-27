package com.hoanien.controllers.api;

import com.hoanien.models.Account;
import com.hoanien.models.Employee;
import com.hoanien.models.StaffSchedule;
import com.hoanien.repositories.AccountRepository;
import com.hoanien.repositories.EmployeeRepository;
import com.hoanien.repositories.StaffScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees/me")
public class EmployeeScheduleApiController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StaffScheduleRepository staffScheduleRepository;

    @GetMapping("/schedules")
    public ResponseEntity<?> getMySchedules() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = auth.getName();
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) {
            return ResponseEntity.status(404).body("Account not found");
        }

        Employee employee = employeeRepository.findByAccountId(account.getId()).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(404).body("Employee profile not found");
        }

        List<StaffSchedule> schedules = staffScheduleRepository.findByEmployeeIdAndWorkDateGreaterThanEqual(employee.getId(), LocalDate.now());
        
        // Cần map ra DTO để tránh vòng lặp JSON vô hạn (infinite recursion) nếu có quan hệ hai chiều
        var response = schedules.stream().map(s -> Map.of(
                "workDate", s.getWorkDate().toString(),
                "shiftName", s.getShift().getShiftName(),
                "startTime", s.getShift().getStartTime().toString(),
                "endTime", s.getShift().getEndTime().toString(),
                "status", s.getStatus() != null ? s.getStatus() : "N/A"
        )).toList();

        return ResponseEntity.ok(response);
    }
}
