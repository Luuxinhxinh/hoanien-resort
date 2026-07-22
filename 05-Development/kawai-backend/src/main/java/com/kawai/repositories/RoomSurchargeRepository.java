package com.kawai.repositories;

import com.kawai.models.RoomCategory;
import com.kawai.models.RoomSurcharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface RoomSurchargeRepository extends JpaRepository<RoomSurcharge, Long> {

    @Query("SELECT rs FROM RoomSurcharge rs WHERE rs.category = :category AND rs.isActive = true AND :age BETWEEN rs.ageFrom AND rs.ageTo")
    Optional<RoomSurcharge> findSurchargeForAge(@Param("category") RoomCategory category, @Param("age") int age);
}
