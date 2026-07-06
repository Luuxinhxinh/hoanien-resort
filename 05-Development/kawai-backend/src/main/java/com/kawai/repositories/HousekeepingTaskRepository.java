package com.kawai.repositories;

import com.kawai.models.HotelOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HousekeepingTaskRepository extends JpaRepository<HotelOperation, Long> {
    List<HotelOperation> findByStatus(String status);

    @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.status = :status ORDER BY CASE h.priority WHEN 'Super High' THEN 1 WHEN 'Normal' THEN 2 WHEN 'Low' THEN 3 ELSE 4 END ASC, h.createdAt DESC")
    List<HotelOperation> findPendingTasksSorted(@org.springframework.data.repository.query.Param("status") String status);

    List<HotelOperation> findByOperationalTypeAndStatus(String operationalType, String status);

    @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.operationalType = :operationalType AND h.status = :status ORDER BY CASE h.priority WHEN 'Super High' THEN 1 WHEN 'Normal' THEN 2 WHEN 'Low' THEN 3 ELSE 4 END ASC, h.createdAt DESC")
    List<HotelOperation> findByOperationalTypeAndStatusSorted(@org.springframework.data.repository.query.Param("operationalType") String operationalType, @org.springframework.data.repository.query.Param("status") String status);

    @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.operationalType IN :operationalTypes AND h.status = :status ORDER BY CASE h.priority WHEN 'Super High' THEN 1 WHEN 'High' THEN 2 WHEN 'Normal' THEN 3 WHEN 'Low' THEN 4 ELSE 5 END ASC, h.createdAt DESC")
    List<HotelOperation> findByOperationalTypesAndStatusSorted(@org.springframework.data.repository.query.Param("operationalTypes") List<String> operationalTypes, @org.springframework.data.repository.query.Param("status") String status);

    @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.room.roomNumber = :roomNumber AND h.status = :status AND h.operationalType = :operationalType")
    List<HotelOperation> findByRoomNumberAndStatusAndType(@org.springframework.data.repository.query.Param("roomNumber") String roomNumber, @org.springframework.data.repository.query.Param("status") String status, @org.springframework.data.repository.query.Param("operationalType") String operationalType);
}
