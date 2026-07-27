package com.hoanien.repositories;

import com.hoanien.models.HotelOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelOperationRepository extends JpaRepository<HotelOperation, Long> {
    List<HotelOperation> findByStatus(String status);
}
