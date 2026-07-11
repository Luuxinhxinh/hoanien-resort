package com.kawai.controllers.api;

import com.kawai.models.Employee;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.models.StaffSchedule;
import com.kawai.repositories.StaffScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/schedules")
public class StaffScheduleRestController {

    private final StaffScheduleRepository staffScheduleRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public StaffScheduleRestController(StaffScheduleRepository staffScheduleRepository,
            EmployeeRepository employeeRepository) {
        this.staffScheduleRepository = staffScheduleRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Endpoint: POST /api/v1/schedules/{id}/close
     * Đóng ca làm việc cho nhân viên.
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> closeShift(@PathVariable("id") Long id) {
        Optional<StaffSchedule> opt = staffScheduleRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StaffSchedule schedule = opt.get();
        schedule.setIsClosed(true);
        staffScheduleRepository.save(schedule);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Đã đóng ca làm việc thành công.");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint: POST /api/v1/schedules/close-current-shift
     * Đóng ca làm việc hiện tại của nhân viên đang đăng nhập.
     */
    @PostMapping("/close-current-shift")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> closeCurrentShift() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Employee> empOpt = employeeRepository.findByAccountUsername(username);
        
        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "Không tìm thấy thông tin nhân viên."));
        }
        
        Employee emp = empOpt.get();
        List<StaffSchedule> todaySchedules = staffScheduleRepository.findByEmployeeIdAndWorkDate(emp.getId(), LocalDate.now());
        
        if (todaySchedules.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "Bạn không có ca làm việc nào trong ngày hôm nay."));
        }
        
        boolean closedAny = false;
        for (StaffSchedule s : todaySchedules) {
            if ("Published".equals(s.getStatus()) && (s.getIsClosed() == null || !s.getIsClosed())) {
                s.setIsClosed(true);
                staffScheduleRepository.save(s);
                closedAny = true;
            }
        }
        
        if (!closedAny) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "Tất cả ca làm việc hôm nay của bạn đã được đóng trước đó."));
        }

        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Đã đóng ca làm việc thành công."));
    }
}
