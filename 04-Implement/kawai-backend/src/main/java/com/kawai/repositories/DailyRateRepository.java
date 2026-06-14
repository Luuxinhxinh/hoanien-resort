package com.kawai.repositories;

import com.kawai.models.DailyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyRateRepository extends JpaRepository<DailyRate, Long> {

    @Query("SELECT d FROM DailyRate d WHERE d.rateDate >= :startDate AND d.rateDate <= :endDate")
    List<DailyRate> findActiveRates(LocalDate startDate, LocalDate endDate);
}