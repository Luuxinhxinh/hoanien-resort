package com.kawai.repositories;

import com.kawai.models.TableReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {
}
