package com.kawai.repositories;

import com.kawai.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);

    @Query("SELECT r.roomStatus, COUNT(r) FROM Room r GROUP BY r.roomStatus")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(r) FROM Room r")
    long countTotalRooms();

    @Query("SELECT r.category.categoryName, COUNT(r) FROM Room r GROUP BY r.category.categoryName")
    List<Object[]> countByCategory();

    @Query("SELECT r FROM Room r WHERE r.roomStatus = 'Occupied'")
    List<Room> findOccupied();

    @Query("SELECT r FROM Room r WHERE r.roomStatus = 'Vacant_Clean' OR r.roomStatus = 'Vacant_Dirty'")
    List<Room> findVacant();
}