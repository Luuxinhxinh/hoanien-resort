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
     * @param bookingId      ID booking cần kiểm tra
     * @param cccdEncrypted  Giá trị CCCD đã mã hoá AES-256
     * @return số bản ghi trùng (0 = chưa có, > 0 = đã tồn tại)
     */
    @Query("SELECT COUNT(d) FROM Dependent d " +
           "WHERE d.customer = (SELECT b.customer FROM Booking b WHERE b.id = :bookingId) " +
           "AND d.cccdPassportEncrypted = :cccdEncrypted")
    int countDuplicateInBooking(@Param("bookingId") Long bookingId,
                                @Param("cccdEncrypted") String cccdEncrypted);
}
