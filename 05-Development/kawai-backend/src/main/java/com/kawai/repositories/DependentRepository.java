package com.kawai.repositories;

import com.kawai.models.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DependentRepository — UC16 Register Accompanying Guests
 */
@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {

    List<Dependent> findByCustomer(com.kawai.models.Customer customer);

    /**
     * Kiểm tra trùng lặp CCCD trong cùng một booking (ADR-002).
     * So sánh theo cccd đã mã hoá để không so sánh plaintext.
     *
     * Fix ADR-002: Check đúng scope — chỉ đếm Dependent đã được link vào booking này
     * thông qua RoomGuest → RoomBookingDetail → RoomBooking.
     * Tránh false-positive khi cùng 1 customer có nhiều booking khác nhau.
     *
     * @param bookingId      ID booking cần kiểm tra
     * @param cccdEncrypted  Giá trị CCCD đã mã hoá AES-256
     * @return số bản ghi trùng (0 = chưa có, > 0 = đã tồn tại)
     */
    @Query("SELECT COUNT(d) FROM Dependent d " +
           "WHERE d.cccdPassportEncrypted = :cccdEncrypted " +
           "AND EXISTS (" +
           "  SELECT 1 FROM RoomGuest rg " +
           "  WHERE rg.dependent = d " +
           "  AND rg.roomBookingDetail.roomBooking.id = :bookingId" +
           ")")
    int countDuplicateInBooking(@Param("bookingId") Long bookingId,
                                @Param("cccdEncrypted") String cccdEncrypted);
}
