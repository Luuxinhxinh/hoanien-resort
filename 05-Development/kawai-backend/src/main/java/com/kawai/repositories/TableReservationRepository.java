package com.kawai.repositories;

import com.kawai.models.TableReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {
    java.util.List<TableReservation> findByCustomerOrderByIdDesc(com.kawai.models.Customer customer);
    java.util.List<TableReservation> findByTable_IdAndReserveDateOrderByReserveTimeAsc(Long tableId, java.time.LocalDate reserveDate);
}
