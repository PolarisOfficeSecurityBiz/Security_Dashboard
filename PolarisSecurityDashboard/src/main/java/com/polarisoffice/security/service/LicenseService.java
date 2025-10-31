package com.polarisoffice.security.service;

import com.polarisoffice.security.model.License;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    /** ✅ 서비스 ID로 라이선스 조회 */
    public Optional<License> findByServiceId(Integer serviceId) {
        return licenseRepository.findByService_ServiceId(serviceId);
    }

    /** ✅ 없으면 예외 발생 */
    public License getByServiceId(Integer serviceId) {
        return findByServiceId(serviceId)
                .orElseThrow(() -> new IllegalStateException("해당 서비스에 발급된 라이선스가 없습니다."));
    }

    /** ✅ 라이선스 발급 */
    public License issueForService(Service svc,
                                   String expiryDate,
                                   Integer usageLimit,
                                   String licenseType,
                                   String licenseVersion) {
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

    /** ✅ 최신 SDK 버전 조회 (파일 기반) */
    public String getLatestSdkVersion() {
        try {
            Path sdkDir = Paths.get("/opt/sdk");
            if (!Files.exists(sdkDir) || !Files.isDirectory(sdkDir)) return null;

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

    /* ============================================================
       🧩 대시보드용 — 라이선스 리스트 및 활성 수량 계산
    ============================================================ */

    /** 🔹 전체 라이선스 리스트 (활성 + 만료 포함) */
    public List<LicenseDashboardDTO> getLicenses(String customerId) {
        List<License> allLicenses = licenseRepository.findAll().stream()
                .filter(l -> l.getService() != null
                        && l.getService().getCustomer() != null
                        && Objects.equals(l.getService().getCustomer().getCustomerId(), customerId))
                .toList();

        List<LicenseDashboardDTO> result = new ArrayList<>();
        for (License lic : allLicenses) {
            LocalDate expire = lic.getExpireDate();
            long daysLeft = (expire != null)
                    ? ChronoUnit.DAYS.between(LocalDate.now(), expire)
                    : -1;

            result.add(new LicenseDashboardDTO(
                    lic.getService().getServiceName(),
                    expire,
                    (daysLeft < 0 ? "만료" : "D-" + daysLeft)
            ));
        }
        return result;
    }

    /** 🔹 활성 라이선스 개수 */
    public int countActiveLicenses(String customerId) {
        return licenseRepository.countActiveByCustomer(customerId, LocalDate.now());
    }

    /* ============================
       DTO (대시보드 전용)
    ============================ */
    public record LicenseDashboardDTO(
            String name,          // 서비스명
            LocalDate expireDate, // 만료일
            String dDay           // D-day 계산
    ) {}
}
