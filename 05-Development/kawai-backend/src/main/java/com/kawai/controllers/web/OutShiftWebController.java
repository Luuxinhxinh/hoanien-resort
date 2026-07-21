package com.kawai.controllers.web;

import com.kawai.models.Account;
import com.kawai.models.Employee;
import com.kawai.models.StaffSchedule;
import com.kawai.repositories.AccountRepository;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.StaffScheduleRepository;
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
