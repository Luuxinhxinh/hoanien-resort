package com.kawai.repositories;

import com.kawai.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    java.util.List<Employee> findAllByAccountUsername(String username);
    java.util.List<Employee> findAllByAccountId(Long accountId);
    java.util.List<Employee> findAllByEmail(String email);

    default java.util.Optional<Employee> findByAccountUsername(String username) {
        java.util.List<Employee> list = findAllByAccountUsername(username);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    default java.util.Optional<Employee> findByAccountId(Long accountId) {
        java.util.List<Employee> list = findAllByAccountId(accountId);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    default java.util.Optional<Employee> findByEmail(String email) {
        java.util.List<Employee> list = findAllByEmail(email);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    boolean existsByCccd(String cccd);
    boolean existsByEmail(String email);
}
