// src/main/java/com/polarisoffice/security/service/LicenseService.java
package com.polarisoffice.security.service;

import com.polarisoffice.security.model.License;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    /** 컨트롤러에서 Optional로 안전 조회 */
    public Optional<License> findByServiceId(Integer serviceId) {
        return licenseRepository.findByService_ServiceId(serviceId);
    }

    /** 필요 시 강제 조회(없으면 예외) */
    public License getByServiceId(Integer serviceId) {
        return findByServiceId(serviceId)
                .orElseThrow(() -> new IllegalStateException("해당 서비스에 발급된 라이선스가 없습니다."));
    }

    /** 발급 로직 예시 */
    public License issueForService(Service svc,
                                   String expiryDate, Integer usageLimit,
                                   String licenseType, String licenseVersion) {
        // 라이선스키 생성 예시
        String key = "LIC-" + UUID.randomUUID();

        License lic = License.builder()
                .service(svc)
                .licenseKey(key)
                .expireDate(expiryDate != null ? LocalDate.parse(expiryDate) : null)
                .useLimit(usageLimit != null ? usageLimit : 2)
                .licenseType(licenseType)
                .createDate(LocalDateTime.now())
                .build();

        // licenseVersion은 컬럼이 없다면 저장 안 해도 되고,
        // 별도 컬럼/테이블이 있으면 맞게 저장하세요.

        return licenseRepository.save(lic);
    }
}
