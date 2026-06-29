package com.kawai.repositories;
import com.kawai.models.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    java.util.List<RefundRequest> findByStatusOrderByCreatedAtDesc(String status);
}
