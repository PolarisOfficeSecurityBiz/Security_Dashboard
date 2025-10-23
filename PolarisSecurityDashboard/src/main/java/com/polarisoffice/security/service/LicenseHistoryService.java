// src/main/java/com/polarisoffice/security/service/LicenseHistoryService.java
package com.polarisoffice.security.service;

import com.polarisoffice.security.model.License;
import com.polarisoffice.security.model.LicenseHistory;
import com.polarisoffice.security.repository.LicenseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseHistoryService {

    private final LicenseHistoryRepository repo;

    /** 라이선스ID 기준 최신순 조회 */
    public List<LicenseHistory> findByLicenseIdOrderByCreateAtDesc(Integer licenseId) {
        return repo.findByLicense_LicenseIdOrderByCreateAtDesc(licenseId);
    }

    /** (호환용) 컨트롤러에서 changedAt 이름을 썼다면 이 메서드로 연결해줘 */
    public List<LicenseHistory> findByLicenseIdOrderByChangedAtDesc(Integer licenseId) {
        return findByLicenseIdOrderByCreateAtDesc(licenseId);
    }

    /** 기록 추가 (actor: 사용자 아이디 또는 표시명) */
    public LicenseHistory addHistory(License license, String actorUserId, String message) {
        LicenseHistory h = LicenseHistory.builder()
                .license(license)
                .userId(actorUserId)
                .commitMessage(message)
                .build();
        return repo.save(h);
    }
}
