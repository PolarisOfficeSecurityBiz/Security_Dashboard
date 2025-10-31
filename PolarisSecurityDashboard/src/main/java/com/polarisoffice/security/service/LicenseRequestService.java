package com.polarisoffice.security.service;

import com.polarisoffice.security.model.License;
import com.polarisoffice.security.model.LicenseRequest;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.LicenseRepository;
import com.polarisoffice.security.repository.LicenseRequestRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LicenseRequestService {

    private final LicenseRequestRepository licenseRequestRepository;
    private final LicenseRepository licenseRepository;
    private final LicenseService licenseService;

    /** 요청 생성 */
    public LicenseRequest createRequest(Service service, String email) {
        LicenseRequest req = LicenseRequest.builder()
                .service(service)
                .domain(service.getDomain())
                .requesterEmail(email)
                .status(LicenseRequest.Status.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        return licenseRequestRepository.save(req);
    }

    /** 승인 처리 + 실제 발급 */
    public License approveRequest(Long requestId) {
        LicenseRequest req = licenseRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        req.setStatus(LicenseRequest.Status.APPROVED);
        req.setApprovedAt(LocalDateTime.now());
        licenseRequestRepository.save(req);

        License license = licenseService.issueForService(req.getService(), null, 2, "STANDARD", "v1.0");
        return license;
    }

    /** 중복 요청 방지용 */
    public boolean existsPendingRequest(String domain) {
        return licenseRequestRepository.findByDomainAndStatus(domain, LicenseRequest.Status.PENDING).isPresent();
    }

    public List<LicenseRequest> getPendingRequests() {
        return licenseRequestRepository.findByStatus(LicenseRequest.Status.PENDING);
    }
}
