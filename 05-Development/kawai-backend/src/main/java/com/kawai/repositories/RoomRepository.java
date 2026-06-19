package com.kawai.repositories;

import com.kawai.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.roomNumber = :roomNumber")
    Optional<Room> findByRoomNumberWithLock(@Param("roomNumber") String roomNumber);

    @Query("SELECT r FROM Room r WHERE r.category.categoryName = :categoryName")
    List<Room> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT r.roomStatus, COUNT(r) FROM Room r GROUP BY r.roomStatus")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(r) FROM Room r")
    long countTotalRooms();

    @Query("SELECT r.category.categoryName, COUNT(r) FROM Room r GROUP BY r.category.categoryName")
    List<Object[]> countByCategory();

    @Query("SELECT r FROM Room r WHERE r.roomStatus = 'Occupied'")
    List<Room> findOccupied();

    @Query("SELECT r FROM Room r WHERE r.roomStatus = 'Vacant_Clean'")
    List<Room> findVacant();

    @Query("SELECT rbd.room FROM RoomBookingDetail rbd " +
            "WHERE rbd.roomBooking.customer.account.id = :userId " +
            "AND rbd.roomBooking.bookingStatus IN ('Confirmed', 'Checked_In')")
    Optional<Room> findActiveRoomByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.category.categoryName = :categoryName AND r.roomStatus != 'Maintenance'")
    long countActiveRoomsByCategoryName(@Param("categoryName") String categoryName);
}