package com.kawai.repositories;

import com.kawai.models.FolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository cho FolioItem entity.
 * Lưu vết các khoản phí (ăn uống, tour, ...) được ghi nợ vào Folio phòng.
 */
@Repository
public interface FolioItemRepository extends JpaRepository<FolioItem, Long> {
    List<FolioItem> findByRoomBookingDetailId(Long roomBookingDetailId);

    List<FolioItem> findByBookingId(Long bookingId);

    @Query(value = "SELECT amount FROM Folio_Items WHERE room_booking_detail_id = :detailId AND is_settled_separately = 0", nativeQuery = true)
    List<BigDecimal> findAmountsByDetailId(@Param("detailId") Long detailId);

    @Query(value = "SELECT amount FROM Folio_Items WHERE room_booking_detail_id = :detailId AND is_settled_separately = 0 AND amount < 0 AND description LIKE 'Nạp tiền nâng hạn mức%'", nativeQuery = true)
    List<BigDecimal> findCreditDepositAmountsByDetailId(@Param("detailId") Long detailId);
}