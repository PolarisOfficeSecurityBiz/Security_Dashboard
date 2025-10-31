package com.polarisoffice.security.controller;

import com.polarisoffice.security.auth.CustomUserDetails;
import com.polarisoffice.security.model.License;
import com.polarisoffice.security.model.LicenseRequest;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.ServiceRepository;
import com.polarisoffice.security.service.LicenseRequestService;
import com.polarisoffice.security.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseApiController {

    private final LicenseService licenseService;
    private final ServiceRepository serviceRepository;
    private final LicenseRequestService licenseRequestService;

    private static final Logger log = LoggerFactory.getLogger(LicenseApiController.class);

    /**
     * 🆕 [고객사] 라이선스 발급 요청
     * 프론트에서 /api/license/issue 로 POST 요청을 보냄
     */
    @PostMapping("/issue")
    public ResponseEntity<?> issueLicense(@RequestBody Map<String, String> req, Authentication auth) {
        try {
            String serviceName = req.get("serviceName");
            String domain = req.get("domain");
            String email = (auth != null && auth.getPrincipal() instanceof CustomUserDetails user)
                    ? user.getUsername() : "anonymous";

            log.info("📩 라이선스 발급 요청 수신 - serviceName={}, domain={}, email={}", serviceName, domain, email);

            if (domain == null || domain.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "도메인 정보가 누락되었습니다."
                ));
            }

            // ✅ 서비스 조회
            Service service = serviceRepository.findByDomain(domain)
                    .orElseThrow(() -> new IllegalArgumentException("해당 도메인에 연결된 서비스가 없습니다."));

            // ✅ 이미 라이선스가 존재하면 중복 방지
            if (service.getLicenseId() != null) {
                return ResponseEntity.ok(Map.of(
                        "status", "exists",
                        "message", "이미 라이선스가 발급되었습니다.",
                        "licenseId", service.getLicenseId()
                ));
            }

            // ✅ 이미 요청한 대기중 요청이 있으면 중복 방지
            if (licenseRequestService.existsPendingRequest(domain)) {
                return ResponseEntity.ok(Map.of(
                        "status", "pending",
                        "message", "이미 발급 요청이 접수되어 승인 대기 중입니다."
                ));
            }

            // ✅ 요청 등록
            LicenseRequest request = licenseRequestService.createRequest(service, email);

            return ResponseEntity.ok(Map.of(
                    "status", "pending",
                    "message", "라이선스 발급 요청이 등록되었습니다.",
                    "requestId", request.getId()
            ));

        } catch (IllegalArgumentException e) {
            log.error("❌ 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("🔥 서버 오류", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "서버 처리 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * ✅ [관리자] 승인 시 실제 발급 처리
     */
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId) {
        try {
            License license = licenseRequestService.approveRequest(requestId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "라이선스 발급이 완료되었습니다.",
                    "licenseKey", license.getLicenseKey()
            ));
        } catch (Exception e) {
            log.error("승인 처리 중 오류", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "승인 처리 중 오류가 발생했습니다."
            ));
        }
    }
}
