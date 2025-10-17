package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface LicenseRepository extends JpaRepository<License, Integer> {
    Optional<License> findByService_ServiceId(Integer serviceId); // ✅ 엔티티에 service 연관이 있을 때
    Optional<License> findByLicenseKey(String licenseKey);
}
