package com.kawai.repositories;

import com.kawai.models.RoomBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomBookingDetailRepository extends JpaRepository<RoomBookingDetail, Long> {
}
