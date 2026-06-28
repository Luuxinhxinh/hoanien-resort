package com.kawai.repositories;

import com.kawai.models.HotelService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelServiceRepository extends JpaRepository<HotelService, Long> {
    Optional<HotelService> findByServiceNameIgnoreCase(String serviceName);
    List<HotelService> findByIsAvailable(Boolean isAvailable);
}
