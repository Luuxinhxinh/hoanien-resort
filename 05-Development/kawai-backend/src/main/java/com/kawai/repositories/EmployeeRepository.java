package com.kawai.repositories;

import com.kawai.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByCccd(String cccd);
    boolean existsByEmail(String email);
}
