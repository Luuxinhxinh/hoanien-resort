package com.kawai.repositories;

import com.kawai.models.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM RoomCategory c WHERE c.categoryName = :categoryName")
    Optional<RoomCategory> findByCategoryNameWithLock(@Param("categoryName") String categoryName);
}
