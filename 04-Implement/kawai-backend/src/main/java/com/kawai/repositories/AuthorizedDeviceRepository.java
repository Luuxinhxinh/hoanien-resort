package com.kawai.repositories;

import com.kawai.models.AuthorizedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizedDeviceRepository extends JpaRepository<AuthorizedDevice, Long> {
    boolean existsByDeviceCodeAndIsApprovedTrue(String deviceCode);
}
