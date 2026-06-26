package com.kawai.repositories;

import com.kawai.models.RoomBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomBookingDetailRepository extends JpaRepository<RoomBookingDetail, Long> {
    List<RoomBookingDetail> findByRoomBookingId(Long bookingId);

    List<RoomBookingDetail> findByDetailStatus(String detailStatus);
    List<RoomBookingDetail> findByDetailStatusIn(List<String> statuses);
}
