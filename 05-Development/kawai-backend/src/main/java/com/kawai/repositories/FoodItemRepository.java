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

    /**
     * TÍNH NĂNG CHIA THỰC ĐƠN THEO NGÀY
     * Query này lọc ra danh sách các món ăn HỢP LỆ để hiển thị cho một thứ cụ thể trong tuần.
     * 
     * Điều kiện lọc (Toán tử logic):
     * 1. m.isAvailable = true (Món này không bị Tạm ngưng/Ẩn khỏi hệ thống)
     * VÀ
     * 2. (Món này là món Cố định (isAlwaysAvailable = true) 
     *     HOẶC 
     *     Ngày được truy vấn (ví dụ Thứ 2) có nằm trong danh sách các ngày được phép bán của món này).
     */
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND (m.isAlwaysAvailable = true OR :day MEMBER OF m.availableDays)")
    List<MenuItem> findAvailableByDayOfWeek(@org.springframework.data.repository.query.Param("day") java.time.DayOfWeek day);
}
