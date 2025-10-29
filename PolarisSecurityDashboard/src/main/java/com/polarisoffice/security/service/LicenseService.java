// src/main/java/com/polarisoffice/security/service/LicenseService.java
package com.polarisoffice.security.service;

import com.polarisoffice.security.model.License;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    /** 라이선스 발급 로직 예시 */
    public License issueForService(Service svc,
                                   String expiryDate, Integer usageLimit,
                                   String licenseType, String licenseVersion) {
        String key = "LIC-" + UUID.randomUUID();

        License lic = License.builder()
                .service(svc)
                .licenseKey(key)
                .expireDate(expiryDate != null ? LocalDate.parse(expiryDate) : null)
                .useLimit(usageLimit != null ? usageLimit : 2)
                .licenseType(licenseType)
                .createDate(LocalDateTime.now())
                .build();

        return licenseRepository.save(lic);
    }

    // ✅ 최신 SDK 버전 조회 (파일 기반)
    public String getLatestSdkVersion() {
        try {
            // SDK 파일이 저장된 디렉토리 경로 (환경에 맞게 수정)
            Path sdkDir = Paths.get("/opt/sdk");

            if (!Files.exists(sdkDir) || !Files.isDirectory(sdkDir)) {
                return null; // SDK 폴더 없음
            }

            // 디렉토리 내 파일 중 최신 수정일 순으로 정렬 후 첫 번째 반환
            try (Stream<Path> files = Files.list(sdkDir)) {
                return files
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparingLong((Path p) -> p.toFile().lastModified()).reversed())
                        .map(p -> p.getFileName().toString())
                        .findFirst()
                        .orElse(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
