package com.kawai.repositories;

import com.kawai.models.TourStaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourStaffAssignmentRepository extends JpaRepository<TourStaffAssignment, Long> {
}
