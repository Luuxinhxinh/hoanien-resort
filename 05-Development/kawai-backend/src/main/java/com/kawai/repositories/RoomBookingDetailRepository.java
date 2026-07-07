package com.kawai.repositories;

import com.kawai.models.RoomBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomBookingDetailRepository extends JpaRepository<RoomBookingDetail, Long> {
        List<RoomBookingDetail> findByRoomBookingId(Long bookingId);

        List<RoomBookingDetail> findByDetailStatus(String detailStatus);

        List<RoomBookingDetail> findByCustomer(com.kawai.models.Customer customer);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM RoomBookingDetail d WHERE d.customer.id = :customerId OR d.roomBooking.customer.id = :customerId")
    List<RoomBookingDetail> findByAnyCustomerId(@org.springframework.data.repository.query.Param("customerId") Long customerId);

    List<RoomBookingDetail> findByDetailStatusIn(List<String> statuses);

        @org.springframework.data.jpa.repository.Query("SELECT rbd FROM RoomBookingDetail rbd " +
                        "WHERE rbd.roomBooking.customer.account.id = :userId " +
                        "AND rbd.roomBooking.bookingStatus IN ('Confirmed', 'Checked_In')")
        List<RoomBookingDetail> findActiveDetailsByUserId(
                        @org.springframework.data.repository.query.Param("userId") Long userId);

        /**
         * Đếm số slot phòng đang bị "giữ chỗ" bởi Confirmed booking nhưng chưa gán
         * phòng vật lý,
         * nhóm theo tên category.
         */
        @Query("SELECT rbd.category.categoryName, COUNT(rbd) " +
                        "FROM RoomBookingDetail rbd " +
                        "WHERE rbd.room IS NULL " +
                        "AND rbd.detailStatus NOT IN ('CHECKED_IN', 'CANCELLED', 'NO_SHOW') " +
                        "AND rbd.roomBooking.bookingStatus = 'Confirmed' " +
                        "GROUP BY rbd.category.categoryName")
        List<Object[]> countPendingUnassignedByCategoryName();

        /**
         * Trả về các phòng đã dọn sạch (Vacant_Clean) cần bàn giao cho khách, gồm 2 case:
         *
         * 1. CONFIRMED + Vacant_Clean + checkInDate <= hôm nay
         *    → Booking online đã confirmed, phòng vừa được dọn, khách chờ nhận phòng
         *
         * 2. CHECKED_IN + Vacant_Clean
         *    → Khách đã check-in (WalkIn trả tiền xong) nhưng phòng vẫn đang dọn,
         *      guest ngồi chờ ở sảnh; house dọn xong → cần báo lễ tân dẫn khách lên
         */
        @org.springframework.data.jpa.repository.Query(
                "SELECT rbd.room.roomNumber, rbd.roomBooking.customer.fullName " +
                "FROM RoomBookingDetail rbd " +
                "WHERE rbd.room IS NOT NULL AND rbd.room.roomStatus = 'Vacant_Clean' " +
                "AND (" +
                "  (UPPER(rbd.detailStatus) = 'CONFIRMED' AND rbd.roomBooking.checkInDate <= CURRENT_DATE) " +
                "  OR " +
                "  UPPER(rbd.detailStatus) = 'CHECKED_IN'" +
                ")")
        List<Object[]> findCleanedRoomsPendingHandover();
}
