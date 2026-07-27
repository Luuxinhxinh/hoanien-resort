package com.hoanien.repositories;

import com.hoanien.models.TourStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourStaffAssignmentRepository extends JpaRepository<TourStaffAssignment, Long> {
    List<TourStaffAssignment> findByScheduleId(Long scheduleId);

    List<TourStaffAssignment> findByEmployeeId(Long employeeId);
    List<TourStaffAssignment> findByEmployeeIdAndSchedule_DepartureDate(Long employeeId, java.time.LocalDate date);
    java.util.Optional<TourStaffAssignment> findFirstBySchedule(com.hoanien.models.TourSchedule schedule);
}
