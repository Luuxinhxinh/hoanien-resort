package com.kawai.services.jobs;

import com.kawai.models.Employee;
import com.kawai.models.StaffSchedule;
import com.kawai.repositories.StaffScheduleRepository;
import com.kawai.services.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleNotificationJob {

    @Autowired
    private StaffScheduleRepository staffScheduleRepository;

    @Autowired
    private com.kawai.repositories.EmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    // Chạy vào 8h sáng Chủ Nhật hàng tuần
    @Scheduled(cron = "0 0 8 * * SUN")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void sendWeeklySchedules() {
        System.out.println("--- Bắt đầu gửi email Lịch làm việc hàng tuần cho toàn bộ nhân viên ---");
        
        LocalDate today = LocalDate.now();
        // Lấy lịch làm việc từ hôm nay trở đi (ví dụ cho 7 ngày tới)
        List<StaffSchedule> upcomingSchedules = staffScheduleRepository.findByWorkDateGreaterThanEqual(today);

        // Lấy danh sách toàn bộ nhân viên
        List<Employee> allEmployees = employeeRepository.findAll();

        if (allEmployees == null || allEmployees.isEmpty()) {
            System.out.println("Không có nhân viên nào trong hệ thống.");
            return;
        }

        // Gửi email cho từng người, bất kể họ có lịch hay không
        for (Employee emp : allEmployees) {
            if (emp.getEmail() == null || emp.getEmail().isEmpty()) {
                continue;
            }

            // Lọc ra lịch của nhân viên này
            List<StaffSchedule> employeeSchedules = upcomingSchedules.stream()
                    .filter(s -> s.getEmployee() != null && s.getEmployee().getId().equals(emp.getId()))
                    .collect(Collectors.toList());
            
            // Sắp xếp lịch theo ngày
            employeeSchedules.sort((s1, s2) -> s1.getWorkDate().compareTo(s2.getWorkDate()));
            
            try {
                emailService.sendWeeklyScheduleEmail(emp.getEmail(), emp.getFullName(), employeeSchedules);
                System.out.println("Đã gửi email thông báo lịch làm việc cho: " + emp.getEmail());
            } catch (Exception e) {
                System.err.println("Lỗi gửi email cho " + emp.getEmail() + ": " + e.getMessage());
            }
        }
        System.out.println("--- Hoàn tất gửi email Lịch làm việc ---");
    }

//    // Lắng nghe sự kiện chạy ứng dụng lần đầu để demo/test
//    @EventListener(ApplicationReadyEvent.class)
//    @org.springframework.core.annotation.Order(2)
//    @org.springframework.transaction.annotation.Transactional(readOnly = true)
//    public void onApplicationReady() {
//        System.out.println("Application Ready: Triggering Schedule Notification (Demo)...");
//        // Giả lập gửi lịch làm việc ngay khi khởi động server
//        sendWeeklySchedules();
//    }
}
