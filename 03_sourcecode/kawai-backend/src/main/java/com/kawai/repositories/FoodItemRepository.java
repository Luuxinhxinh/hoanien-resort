package com.kawai.repositories;

import com.kawai.models.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<MenuItem, Long> {
    
    @Query("SELECT m.category, COUNT(m) FROM MenuItem m GROUP BY m.category")
    List<Object[]> findCategoryCounts();
    
    @Query("SELECT DISTINCT m.category FROM MenuItem m")
    List<String> findDistinctCategories();
}
