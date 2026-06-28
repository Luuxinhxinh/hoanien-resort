package com.kawai.repositories;

import com.kawai.models.HotelOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<HotelOperation, Long> {
    List<HotelOperation> findByOperationalType(String operationalType);
    List<HotelOperation> findByOperationalTypeAndStatus(String operationalType, String status);
}
