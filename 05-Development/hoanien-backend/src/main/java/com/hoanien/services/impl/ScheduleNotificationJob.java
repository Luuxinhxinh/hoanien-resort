package com.hoanien.services.impl;

import com.hoanien.models.Employee;
import com.hoanien.models.StaffSchedule;
import com.hoanien.repositories.StaffScheduleRepository;
import com.hoanien.services.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleNotificationJob {

    @Autowired
    private StaffScheduleRepository staffScheduleRepository;

    @Autowired
    private com.hoanien.repositories.EmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    // Chạy vào 8h sáng Chủ Nhật hàng tuần
    @Scheduled(cron = "0 0 8 * * SUN")
    public void sendWeeklySchedules() {
        System.out.println("--- Bắt đầu quét và gửi Lịch làm việc hàng tuần ---");

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
        // Thêm delay 200ms giữa các email để tránh bị Gmail SMTP throttle khi gửi nhiều email liên tiếp
        int emailCount = 0;
        for (Employee emp : allEmployees) {
            if (emp.getEmail() == null || emp.getEmail().isEmpty()) {
                continue;
            }

            // [MÔ PHỎNG] Chỉ gửi cho account lelinh, các tài khoản khác bỏ qua
            if (emp.getAccount() == null || !"lelinh".equals(emp.getAccount().getUsername())) {
                continue;
            }

            // Lọc ra lịch của nhân viên này
            List<StaffSchedule> employeeSchedules = upcomingSchedules.stream()
                    .filter(s -> s.getEmployee() != null && s.getEmployee().getId().equals(emp.getId()))
                    .collect(Collectors.toList());

            // Sắp xếp lịch theo ngày
            employeeSchedules.sort((s1, s2) -> s1.getWorkDate().compareTo(s2.getWorkDate()));

            // Fix LazyInitializationException: Khởi tạo dữ liệu Shift trước khi truyền vào
            // hàm @Async
            for (StaffSchedule s : employeeSchedules) {
                if (s.getShift() != null) {
                    s.getShift().getShiftName();
                    s.getShift().getStartTime();
                    s.getShift().getEndTime();
                }
            }

            try {
                emailService.sendWeeklyScheduleEmail(emp.getEmail(), emp.getFullName(), employeeSchedules);
                System.out.println("Đã gửi email thông báo lịch làm việc cho: " + emp.getEmail());
                emailCount++;
                // Rate limit: delay 200ms mỗi email để tránh Gmail throttle
                if (emailCount % 5 == 0) {
                    Thread.sleep(1000); // Nghỉ 1 giây sau mỗi 5 email
                } else {
                    Thread.sleep(200);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println("Schedule notification bị gián đoạn!");
                break;
            } catch (Exception e) {
                System.err.println("Lỗi gửi email cho " + emp.getEmail() + ": " + e.getMessage());
            }
        }
        System.out.println("--- Hoàn tất gửi email Lịch làm việc (" + emailCount + " emails sent) ---");
    }

    // Lắng nghe sự kiện chạy ứng dụng lần đầu để demo/test
    @EventListener(ApplicationReadyEvent.class)
    @org.springframework.core.annotation.Order(2)
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void onApplicationReady() {
        System.out.println("Application Ready: Triggering Schedule Notification (Demo)...");
        // Giả lập gửi lịch làm việc ngay khi khởi động server
        sendWeeklySchedules();
    }
}
