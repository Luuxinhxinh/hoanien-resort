package com.hoanien.repositories;

import com.hoanien.models.FnBDailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FnBDailyReportRepository extends JpaRepository<FnBDailyReport, Long> {
    Optional<FnBDailyReport> findByReportDate(LocalDate reportDate);
    boolean existsByReportDate(LocalDate reportDate);
}
