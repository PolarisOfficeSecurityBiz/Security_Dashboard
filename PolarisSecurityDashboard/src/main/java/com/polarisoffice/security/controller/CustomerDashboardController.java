package com.polarisoffice.security.controller;

import com.polarisoffice.security.auth.CustomUserDetails;
import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.service.CustomerInfoService;
import com.polarisoffice.security.service.LicenseService;
import com.polarisoffice.security.service.LogService;
import com.polarisoffice.security.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CustomerDashboardController {

    private final CustomerInfoService customerInfoService;
    private final LogService userLogService;
    private final SettlementService settlementService;
    private final LicenseService licenseService;

    @GetMapping("/customer/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = ((CustomUserDetails) auth.getPrincipal()).getUsername();
        Customer customer = customerInfoService.getCustomerInfo(username);

        // ✅ 이번달 유입 유저 수
        int joinCount = userLogService.countMonthlyJoin(customer.getCustomerId());

        // ✅ 이번달 정산 금액
        long totalSettlement = settlementService.getMonthlyTotal(customer.getCustomerId(), LocalDate.now().getMonthValue());

        // ✅ 활성 라이선스 수
        int activeLicenses = licenseService.countActiveLicenses(customer.getCustomerId());

        // ✅ 월별 유입 현황 (차트용)
        Map<String, Integer> monthlyJoinMap = userLogService.getYearlyJoinCount(customer.getCustomerId());

        // ✅ 올해 정산 내역
        List<Map<String, Object>> settlements = settlementService.getYearlySettlements(customer.getCustomerId());

        // ✅ 라이선스 상태
        var licenses = licenseService.getLicenses(customer.getCustomerId());

        model.addAttribute("joinCount", joinCount);
        model.addAttribute("totalSettlement", totalSettlement);
        model.addAttribute("activeLicenses", activeLicenses);
        model.addAttribute("monthlyJoin", monthlyJoinMap);
        model.addAttribute("settlements", settlements);
        model.addAttribute("licenses", licenses);

        return "customer/dashboard";
    }
}
