package com.kawai.repositories;

import com.kawai.models.StaffSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, Long> {
    List<StaffSchedule> findByWorkDate(LocalDate workDate);
    List<StaffSchedule> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
    List<StaffSchedule> findByWorkDateAndStatus(LocalDate workDate, String status);
    
    List<StaffSchedule> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(Long employeeId, LocalDate startDate, LocalDate endDate);
    
    void deleteByWorkDateBetween(LocalDate startDate, LocalDate endDate);
    boolean existsByWorkDate(LocalDate date);
}
