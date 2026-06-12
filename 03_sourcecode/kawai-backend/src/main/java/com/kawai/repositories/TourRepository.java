package com.kawai.repositories;

import com.kawai.models.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    
    @Query("SELECT t.tourType, COUNT(t) FROM Tour t GROUP BY t.tourType")
    List<Object[]> findCategoryCounts();
    
    @Query("SELECT DISTINCT t.tourType FROM Tour t")
    List<String> findDistinctCategories();
}
