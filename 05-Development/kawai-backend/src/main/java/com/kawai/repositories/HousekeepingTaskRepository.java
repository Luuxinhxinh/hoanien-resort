package com.kawai.repositories;

import com.kawai.models.HotelOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HousekeepingTaskRepository extends JpaRepository<HotelOperation, Long> {
        List<HotelOperation> findByStatus(String status);

        @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.status = :status ORDER BY CASE h.priority WHEN 'Lễ tân báo dọn khẩn' THEN 0 WHEN 'Lễ tân báo dọn khẩn cấp' THEN 1 WHEN 'Urgent' THEN 2 WHEN 'Super High' THEN 3 WHEN 'High' THEN 4 WHEN 'Normal' THEN 5 WHEN 'Low' THEN 6 ELSE 7 END ASC, h.createdAt DESC")
        List<HotelOperation> findPendingTasksSorted(
                        @org.springframework.data.repository.query.Param("status") String status);

        List<HotelOperation> findByOperationalTypeAndStatus(String operationalType, String status);

        @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.operationalType = :operationalType AND h.status = :status ORDER BY CASE h.priority WHEN 'Lễ tân báo dọn khẩn' THEN 0 WHEN 'Lễ tân báo dọn khẩn cấp' THEN 1 WHEN 'Urgent' THEN 2 WHEN 'Super High' THEN 3 WHEN 'High' THEN 4 WHEN 'Normal' THEN 5 WHEN 'Low' THEN 6 ELSE 7 END ASC, h.createdAt DESC")
        List<HotelOperation> findByOperationalTypeAndStatusSorted(
                        @org.springframework.data.repository.query.Param("operationalType") String operationalType,
                        @org.springframework.data.repository.query.Param("status") String status);

        @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.operationalType IN :operationalTypes AND h.status = :status ORDER BY CASE h.priority WHEN 'Lễ tân báo dọn khẩn' THEN 0 WHEN 'Lễ tân báo dọn khẩn cấp' THEN 1 WHEN 'Urgent' THEN 2 WHEN 'Super High' THEN 3 WHEN 'High' THEN 4 WHEN 'Normal' THEN 5 WHEN 'Low' THEN 6 ELSE 7 END ASC, h.createdAt DESC")
        List<HotelOperation> findByOperationalTypesAndStatusSorted(
                        @org.springframework.data.repository.query.Param("operationalTypes") List<String> operationalTypes,
                        @org.springframework.data.repository.query.Param("status") String status);

        @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h WHERE h.room.roomNumber = :roomNumber AND h.status = :status AND h.operationalType = :operationalType")
        List<HotelOperation> findByRoomNumberAndStatusAndType(
                        @org.springframework.data.repository.query.Param("roomNumber") String roomNumber,
                        @org.springframework.data.repository.query.Param("status") String status,
                        @org.springframework.data.repository.query.Param("operationalType") String operationalType);

        @org.springframework.data.jpa.repository.Query("SELECT h FROM HotelOperation h " +
                        "WHERE h.operationalType IN ('URGENT_CLEAN', 'CHECKOUT_CLEAN') " +
                        "AND h.status = 'Completed' " +
                        "AND h.completedAt >= :since")
        List<HotelOperation> findRecentCompletedCleanTasks(
                        @org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);
}
