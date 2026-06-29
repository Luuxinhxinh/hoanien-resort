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
        * 
        * @param bookingId     ID booking cần kiểm tra
        * @param cccdEncrypted Giá trị CCCD đã mã hoá AES-256
        * @return số bản ghi trùng (0 = chưa có, > 0 = đã tồn tại)
        */
       @Query("SELECT COUNT(rg) FROM RoomGuest rg " +
                     "WHERE rg.roomBookingDetail.roomBooking.id = :bookingId " +
                     "AND rg.dependent IS NOT NULL " +
                     "AND rg.dependent.cccdPassportEncrypted = :cccdEncrypted")
       int countDuplicateInBooking(@Param("bookingId") Long bookingId,
                     @Param("cccdEncrypted") String cccdEncrypted);
}
