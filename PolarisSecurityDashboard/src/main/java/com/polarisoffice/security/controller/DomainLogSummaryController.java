package com.polarisoffice.security.controller;

import com.polarisoffice.security.auth.CustomUserDetails;
import com.polarisoffice.security.dto.LogListItem;
import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.model.SecuOneLogEvent;
import com.polarisoffice.security.repository.SecuOneLogEventRepository;
import com.polarisoffice.security.service.CustomerInfoService;
import com.polarisoffice.security.service.LogService;
import com.polarisoffice.security.service.ServiceContactService;
import com.polarisoffice.security.service.ServiceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DomainLogSummaryController {

    private final SecuOneLogEventRepository secuOneRepo;
    private final LogService logService;
    private final ServiceService serviceService;
    private final ServiceContactService contactService;
    private final CustomerInfoService customerInfoService;

    @GetMapping("/customer/domain-summary")
    public String showDomainSummary(Authentication authentication, Model model) {
        // ✅ 1️⃣ 로그인 사용자 정보 추출
        String email = ((CustomUserDetails) authentication.getPrincipal()).getUsername();

        // ✅ 2️⃣ 담당자 → 고객사 → 서비스 → 도메인
        ServiceContact contact = contactService.getByEmail(email);
        Customer customer = contact.getCustomer();
        Service service = serviceService.getPrimaryService(customer.getCustomerId());

        if (service == null || service.getDomain() == null) {
            model.addAttribute("error", "서비스 도메인을 찾을 수 없습니다.");
            return "customer/domain_summary";
        }

        String domain = service.getDomain();

        // ✅ 3️⃣ SecuOne 유입 로그 (utm_source 기준)
        List<SecuOneLogEvent> secuoneLogs = secuOneRepo.findAll().stream()
                .filter(e -> e.getUtmSource() != null)
                .filter(e -> e.getUtmSource().equalsIgnoreCase(domain))
                .collect(Collectors.toList());

        long userCount = secuoneLogs.size();

        // ✅ 4️⃣ LogEntry (보안 로그)
        List<LogListItem> logEntries = logService.getLogsByExactDomain(domain).stream()
                .sorted(Comparator.comparing(LogListItem::createdAt).reversed())
                .collect(Collectors.toList());

        // ✅ 5️⃣ 모델 데이터
        model.addAttribute("domain", domain);
        model.addAttribute("userCount", userCount);
        model.addAttribute("logEntries", logEntries);

        System.out.printf("✅ [도메인 요약] user=%s | domain=%s | 유입:%d | 로그:%d%n",
                email, domain, userCount, logEntries.size());

        return "customer/domain_summary";
    }
}
