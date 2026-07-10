package com.kawai.services;

import com.kawai.models.Employee;
import java.time.LocalDate;
import java.util.List;

public interface ShiftService {
    /**
     * Lấy danh sách nhân viên đang có ca trực (Published) trong một ngày cụ thể, lọc theo role.
     * @param roleName Tên role (vd: "TOURGUIDE", "HOUSEKEEPING")
     * @param date Ngày cần kiểm tra
     * @return Danh sách Employee
     */
    List<Employee> getAvailableStaffByRoleAndDate(String roleName, LocalDate date);
    
    /**
     * Lấy một nhân viên ngẫu nhiên (hoặc theo logic phân bổ) đang trực trong ngày, lọc theo role.
     * Fallback: Nếu không có ai trực, lấy một nhân viên bất kỳ có role đó.
     * @param roleName Tên role
     * @param date Ngày cần kiểm tra
     * @return Employee hoặc null nếu không có ai
     */
    Employee assignTaskToAvailableStaff(String roleName, LocalDate date);

    /**
     * Gán Hướng dẫn viên cho một lịch trình Tour (chống trùng lịch).
     */
    Employee assignGuideToTour(com.kawai.models.TourSchedule schedule);
}
