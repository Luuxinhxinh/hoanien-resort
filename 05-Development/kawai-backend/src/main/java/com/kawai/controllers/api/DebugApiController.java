package com.kawai.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kawai.models.Employee;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.services.interfaces.EmailService;
import java.util.List;

@RestController
public class DebugApiController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/guest/test-email")
    public String debugTestEmail() {
        StringBuilder sb = new StringBuilder();
        try {
            List<Employee> emps = employeeRepository.findAll();
            Employee leLinh = emps.stream().filter(e -> "Lê Linh".equals(e.getFullName())).findFirst().orElse(null);
            if (leLinh == null) {
                return "Không tìm thấy nhân viên Lê Linh trong DB.";
            }
            sb.append("Lê Linh có email: ").append(leLinh.getEmail()).append("<br>");
            
            try {
                emailService.sendEmail(leLinh.getEmail(), "Debug Test", "Đây là email test từ hệ thống.");
                sb.append("Đã gọi hàm sendEmail thành công! Hãy kiểm tra hòm thư ").append(leLinh.getEmail()).append("<br>");
            } catch (Exception e) {
                sb.append("Lỗi khi gửi mail: ").append(e.getMessage()).append("<br>");
                for (StackTraceElement el : e.getStackTrace()) {
                    sb.append(el.toString()).append("<br>");
                }
            }
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
        return sb.toString();
    }
}
