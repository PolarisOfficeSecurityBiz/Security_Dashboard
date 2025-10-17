// src/main/java/com/polarisoffice/security/repository/LicenseHistoryRepository.java
package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Integer> {

    // 라이선스별 히스토리, 최신순
    List<LicenseHistory> findByLicense_LicenseIdOrderByCreateAtDesc(Integer licenseId);

    // 필요 시: 최근 N개
    List<LicenseHistory> findTop100ByLicense_LicenseIdOrderByCreateAtDesc(Integer licenseId);
}
