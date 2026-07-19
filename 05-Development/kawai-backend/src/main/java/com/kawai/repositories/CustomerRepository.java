package com.kawai.repositories;

import com.kawai.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    java.util.List<Customer> findAllByAccount_Username(String username);
    java.util.List<Customer> findAllByEmail(String email);

    default java.util.Optional<Customer> findByAccount_Username(String username) {
        java.util.List<Customer> list = findAllByAccount_Username(username);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    default java.util.Optional<Customer> findByEmail(String email) {
        java.util.List<Customer> list = findAllByEmail(email);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    boolean existsByEmail(String email);
    Optional<Customer> findFirstByCccdPassportEncrypted(String cccdPassportEncrypted);
    Optional<Customer> findFirstByPhone(String phone);
}

