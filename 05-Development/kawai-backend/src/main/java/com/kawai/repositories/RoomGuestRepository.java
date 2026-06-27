package com.kawai.repositories;

import com.kawai.models.RoomGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomGuestRepository extends JpaRepository<RoomGuest, Long> {
    List<RoomGuest> findByRoomBookingDetailId(Long detailId);
    java.util.Optional<RoomGuest> findByDependentId(Long dependentId);
    java.util.Optional<RoomGuest> findByCustomerIdAndGuestType(Long customerId, String guestType);
    List<RoomGuest> findByCustomerId(Long customerId);
    java.util.Optional<RoomGuest> findByRoomBookingDetailIdAndIsPrimaryContactTrue(Long detailId);
}
