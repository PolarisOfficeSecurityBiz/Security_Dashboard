package com.polarisoffice.security.repository;


import com.polarisoffice.security.model.LicenseRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRequestRepository extends JpaRepository<LicenseRequest, Long> {
    List<LicenseRequest> findByStatus(LicenseRequest.Status status);
    Optional<LicenseRequest> findByDomainAndStatus(String domain, LicenseRequest.Status status);
}