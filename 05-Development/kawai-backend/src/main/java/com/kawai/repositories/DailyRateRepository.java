package com.kawai.repositories;

import com.kawai.models.DailyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyRateRepository extends JpaRepository<DailyRate, Long> {

    @Query("SELECT d FROM DailyRate d WHERE d.rateDate >= :startDate AND d.rateDate <= :endDate")
    List<DailyRate> findActiveRates(LocalDate startDate, LocalDate endDate);

    java.util.Optional<DailyRate> findByCategoryIdAndRateDate(Long categoryId, LocalDate rateDate);

    List<DailyRate> findByCategoryIdAndRateDateBetween(Long categoryId, LocalDate startDate, LocalDate endDate);

    @Transactional
    @Modifying
    @Query("DELETE FROM DailyRate d WHERE d.rateDate < :date")
    void deleteByRateDateBefore(@Param("date") LocalDate date);
}