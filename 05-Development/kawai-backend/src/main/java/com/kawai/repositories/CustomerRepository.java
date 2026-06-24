package com.kawai.repositories;

import com.kawai.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByAccount_Username(String username);
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Customer> findByCccdPassportEncrypted(String cccdPassportEncrypted);
    Optional<Customer> findByPhone(String phone);
}

