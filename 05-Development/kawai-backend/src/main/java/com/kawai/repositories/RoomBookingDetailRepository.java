package com.kawai.repositories;

import com.kawai.models.RoomBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomBookingDetailRepository extends JpaRepository<RoomBookingDetail, Long> {
    List<RoomBookingDetail> findByRoomBookingId(Long bookingId);

    List<RoomBookingDetail> findByDetailStatus(String detailStatus);
    List<RoomBookingDetail> findByCustomer(com.kawai.models.Customer customer);
    List<RoomBookingDetail> findByDetailStatusIn(List<String> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT rbd FROM RoomBookingDetail rbd " +
           "WHERE rbd.roomBooking.customer.account.id = :userId " +
           "AND rbd.roomBooking.bookingStatus IN ('Confirmed', 'Checked_In')")
    List<RoomBookingDetail> findActiveDetailsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
