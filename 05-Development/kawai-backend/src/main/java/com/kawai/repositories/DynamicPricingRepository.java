package com.kawai.repositories;

import com.kawai.models.DynamicPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DynamicPricingRepository extends JpaRepository<DynamicPricing, Long> {
    List<DynamicPricing> findByCategoryId(Long categoryId);
}
