package com.kawai.repositories;

import com.kawai.models.RestaurantTable;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    @Query("SELECT tr.table FROM TableReservation tr " +
            "WHERE tr.customer.account.id = :userId " +
            "AND tr.reserveDate = CURRENT_DATE " +
            "AND (tr.status = 'CONFIRMED' OR tr.status = 'Confirmed')")
    Optional<RestaurantTable> findTodayTableByUserId(@Param("userId") Long userId);
}
