package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.LicenseRequest;
import com.polarisoffice.security.service.LicenseRequestService;
import com.polarisoffice.security.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/license")
@RequiredArgsConstructor
public class AdminLicenseRequestController {

    private final LicenseRequestService licenseRequestService;
    private final ServiceService serviceService;

    /**
     * ✅ 관리자 라이선스 관리 메인 페이지
     * - 발급 요청 목록 (승인 대기)
     * - 미발급 서비스 목록
     * - (선택) 만료 D-30 서비스 목록
     */
    @GetMapping
    public String licenseDashboard(Model model) {

        // 🔹 1. 발급 요청 목록
        List<LicenseRequest> issueRequests = licenseRequestService.getPendingRequests();

        // 🔹 2. 미발급 서비스 목록
        var unissuedServices = serviceService.getUnissuedServices();

        // 🔹 3. (선택) 만료 D-30 서비스
        // var expiringSoon = serviceService.getExpiringSoonServices();

        // ✅ 모델에 데이터 전달
        model.addAttribute("issueRequests", issueRequests);
        model.addAttribute("services", unissuedServices);
        // model.addAttribute("expiringServices", expiringSoon);

        return "admin/license/admin_license";
    }

    /**
     * 🧾 발급 요청만 따로 보는 페이지 (옵션)
     * /admin/license/requests
     */
    @GetMapping("/requests")
    public String pendingRequests(Model model) {
        model.addAttribute("requests", licenseRequestService.getPendingRequests());
        return "admin/license/request_list";
    }

    /**
     * ✅ 관리자 승인 버튼 클릭 시 실제 발급 수행
     * 승인 완료 후 다시 대시보드로 리다이렉트
     */
    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id) {
        licenseRequestService.approveRequest(id);
        return "redirect:/admin/license";
    }
}
