package com.kawai.repositories;

import com.kawai.models.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM RoomCategory c WHERE c.categoryName = :categoryName")
    java.util.List<RoomCategory> findAllByCategoryNameWithLock(@Param("categoryName") String categoryName);

    java.util.List<RoomCategory> findAllByCategoryName(String categoryName);

    default java.util.Optional<RoomCategory> findByCategoryNameWithLock(String categoryName) {
        java.util.List<RoomCategory> list = findAllByCategoryNameWithLock(categoryName);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    default java.util.Optional<RoomCategory> findByCategoryName(String categoryName) {
        java.util.List<RoomCategory> list = findAllByCategoryName(categoryName);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }
}
