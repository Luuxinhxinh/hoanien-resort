package com.hoanien.controllers.web;

import com.hoanien.models.Account;
import com.hoanien.models.Employee;
import com.hoanien.models.StaffSchedule;
import com.hoanien.repositories.AccountRepository;
import com.hoanien.repositories.EmployeeRepository;
import com.hoanien.repositories.StaffScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class OutShiftWebController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StaffScheduleRepository staffScheduleRepository;

    @GetMapping("/out-of-shift")
    public String showOutOfShiftPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Account account = accountRepository.findByUsername(auth.getName()).orElse(null);
            if (account != null) {
                Employee employee = employeeRepository.findByAccountId(account.getId()).orElse(null);
                if (employee != null) {
                    List<StaffSchedule> schedules = staffScheduleRepository.findByEmployeeIdAndWorkDateGreaterThanEqual(employee.getId(), LocalDate.now());
                    model.addAttribute("schedules", schedules);
                }
            }
        }
        return "common/out-of-shift";
    }
}
