package com.kawai.services.impl;

import com.kawai.models.Employee;
import com.kawai.models.StaffSchedule;
import com.kawai.repositories.EmployeeRepository;
import com.kawai.repositories.StaffScheduleRepository;
import com.kawai.services.interfaces.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final StaffScheduleRepository staffScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final com.kawai.repositories.TourStaffAssignmentRepository tourStaffAssignmentRepository;

    @Override
    public List<Employee> getAvailableStaffByRoleAndDate(String roleName, LocalDate date) {
        // Tìm các lịch làm việc đã được "Published" vào ngày này
        List<StaffSchedule> schedules = staffScheduleRepository.findByWorkDateAndStatus(date, "Published");

        // Lọc ra các nhân viên thuộc Role yêu cầu
        return schedules.stream()
                .map(StaffSchedule::getEmployee)
                .filter(employee -> employee.getAccount() != null && employee.getAccount().getRole() != null
                        && roleName.equalsIgnoreCase(employee.getAccount().getRole().getRoleName()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Employee assignTaskToAvailableStaff(String roleName, LocalDate date) {
        List<Employee> availableStaff = getAvailableStaffByRoleAndDate(roleName, date);

        if (!availableStaff.isEmpty()) {
            // Logic đơn giản: chọn người đầu tiên (hoặc có thể cải tiến đếm số task để cân
            // bằng tải)
            return availableStaff.get(0);
        }

        // Fallback: Nếu không ai trực, tìm 1 nhân viên bất kỳ có role đó
        return employeeRepository.findAll().stream()
                .filter(e -> e.getAccount() != null && e.getAccount().getRole() != null
                        && roleName.equalsIgnoreCase(e.getAccount().getRole().getRoleName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Employee assignGuideToTour(com.kawai.models.TourSchedule schedule) {
        if (schedule == null || schedule.getDepartureDate() == null || schedule.getDepartureTime() == null) {
            return null;
        }

        // 1. Kiểm tra xem tour này đã được gán chưa
        java.util.Optional<com.kawai.models.TourStaffAssignment> existingAssignment = tourStaffAssignmentRepository
                .findFirstBySchedule(schedule);
        if (existingAssignment.isPresent()) {
            return existingAssignment.get().getEmployee();
        }

        // Tính toán khoảng thời gian của tour mới
        java.time.LocalTime newStartTime = schedule.getDepartureTime();
        double durationHours = schedule.getTour() != null && schedule.getTour().getDurationHours() != null
                ? schedule.getTour().getDurationHours()
                : 2.0;
        long durationMinutes = (long) (durationHours * 60);
        java.time.LocalTime newEndTime = newStartTime.plusMinutes(durationMinutes);

        // 2. Lấy danh sách nhân viên đang có ca trực
        List<Employee> availableStaff = getAvailableStaffByRoleAndDate("TOURGUIDE", schedule.getDepartureDate());

        Employee selectedGuide = null;

        // 3. Duyệt tìm người rảnh
        for (Employee staff : availableStaff) {
            if (isGuideFree(staff.getId(), schedule.getDepartureDate(), newStartTime, newEndTime)) {
                selectedGuide = staff;
                break;
            }
        }

        // 4. Fallback nếu tất cả đều bận (tìm người rảnh không nằm trong ca trực)
        if (selectedGuide == null) {
            List<Employee> allGuides = employeeRepository.findAll().stream()
                    .filter(e -> e.getAccount() != null && e.getAccount().getRole() != null
                            && "TOURGUIDE".equalsIgnoreCase(e.getAccount().getRole().getRoleName()))
                    .collect(Collectors.toList());
            for (Employee staff : allGuides) {
                if (isGuideFree(staff.getId(), schedule.getDepartureDate(), newStartTime, newEndTime)) {
                    selectedGuide = staff;
                    break;
                }
            }
        }

        // 5. Lưu vào DB nếu tìm được người
        if (selectedGuide != null) {
            com.kawai.models.TourStaffAssignment assignment = new com.kawai.models.TourStaffAssignment();
            assignment.setSchedule(schedule);
            assignment.setEmployee(selectedGuide);
            assignment.setIsLeadGuide(true);
            assignment.setStaffRole("TOURGUIDE");
            tourStaffAssignmentRepository.save(assignment);
        }

        return selectedGuide;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkIsOnShift(org.springframework.security.core.Authentication auth,
            jakarta.servlet.http.HttpSession session) {
        if (session != null && session.getAttribute("demoBypassShift") != null) {
            return true;
        }
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        if (auth != null) {
            Employee currentStaff = employeeRepository.findByAccountUsername(auth.getName()).orElse(null);
            if (currentStaff == null) {
                return true; // Không phải nhân viên (khách) thì không bị chặn ca
            }
            if (currentStaff != null) {
                var shifts = staffScheduleRepository.findByEmployeeIdAndWorkDate(currentStaff.getId(), LocalDate.now());
                if (shifts != null && !shifts.isEmpty()) {
                    java.time.LocalTime now = java.time.LocalTime.now();
                    for (var schedule : shifts) {
                        java.time.LocalTime startTime = schedule.getShift().getStartTime();
                        java.time.LocalTime endTime = schedule.getShift().getEndTime();
                        if (startTime.isBefore(endTime)) {
                            if (!now.isBefore(startTime) && !now.isAfter(endTime)) {
                                return true;
                            }
                        } else {
                            if (!now.isBefore(startTime) || !now.isAfter(endTime)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isGuideFree(Long employeeId, LocalDate date, java.time.LocalTime newStart,
            java.time.LocalTime newEnd) {
        List<com.kawai.models.TourStaffAssignment> todayTours = tourStaffAssignmentRepository
                .findByEmployeeIdAndSchedule_DepartureDate(employeeId, date);
        for (com.kawai.models.TourStaffAssignment task : todayTours) {
            if (task.getSchedule() != null && task.getSchedule().getDepartureTime() != null) {
                java.time.LocalTime taskStart = task.getSchedule().getDepartureTime();
                double taskDuration = task.getSchedule().getTour() != null
                        && task.getSchedule().getTour().getDurationHours() != null
                                ? task.getSchedule().getTour().getDurationHours()
                                : 2.0;
                java.time.LocalTime taskEnd = taskStart.plusMinutes((long) (taskDuration * 60));

                // Kiểm tra xem có giao nhau không
                // Hai đoạn [A, B] và [C, D] giao nhau khi: A < D và C < B
                if (newStart.isBefore(taskEnd) && taskStart.isBefore(newEnd)) {
                    return false; // Bị trùng lịch
                }
            }
        }
        return true; // Rảnh
    }
}
