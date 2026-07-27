package com.hoanien.repositories;

import com.hoanien.models.TableReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {
    java.util.List<TableReservation> findByCustomerOrderByIdDesc(com.hoanien.models.Customer customer);
    java.util.List<TableReservation> findByTable_IdAndReserveDateOrderByReserveTimeAsc(Long tableId, java.time.LocalDate reserveDate);
    java.util.List<TableReservation> findByReserveDate(java.time.LocalDate reserveDate);
}
