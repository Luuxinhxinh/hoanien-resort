package com.kawai.repositories;

import com.kawai.models.Dependent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DependentRepository extends JpaRepository<Dependent, Long> {

}
