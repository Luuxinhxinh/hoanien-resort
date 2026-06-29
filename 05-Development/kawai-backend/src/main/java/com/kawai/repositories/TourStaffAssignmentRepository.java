package com.kawai.repositories;

import com.kawai.models.TourStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourStaffAssignmentRepository extends JpaRepository<TourStaffAssignment, Long> {
    List<TourStaffAssignment> findByScheduleId(Long scheduleId);

    List<TourStaffAssignment> findByEmployeeId(Long employeeId);
}
