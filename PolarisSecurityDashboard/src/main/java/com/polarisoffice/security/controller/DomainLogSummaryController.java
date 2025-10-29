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
    public String showDomainSummary(Model model, Authentication auth) {
        // 로그인한 사용자 이메일 → 고객사 조회
        String email = ((CustomUserDetails) auth.getPrincipal()).getUsername();
        ServiceContact contact = contactService.getByEmail(email);
        Customer customer = contact.getCustomer();

        // 고객의 주요 서비스 조회
        Service service = serviceService.getPrimaryService(customer.getCustomerId());
        String domain = (service != null) ? service.getDomain() : "-";

        // ✅ 시큐원 로그 수집 (utmSource 기준)
        List<SecuOneLogEvent> secuLogs = secuOneLogRepository.findAll().stream()
                .filter(e -> e.getUtmSource() != null && e.getUtmSource().equalsIgnoreCase(domain))
                .toList();

        // ✅ 보안 로그 수집 (domain 기준)
        List<LogEntry> logEntries = logEntryRepository.findAll().stream()
                .filter(l -> l.getDomain() != null && l.getDomain().equalsIgnoreCase(domain))
                .toList();

        // ✅ userCount가 null이면 0 처리
        int userCount = (secuLogs != null) ? secuLogs.size() : 0;

        model.addAttribute("domain", domain);
        model.addAttribute("userCount", userCount);
        model.addAttribute("logEntries", logEntries);

        System.out.printf("✅ [도메인 리포트] domain=%s, 유입=%d명, 로그=%d건%n",
                domain, userCount, logEntries.size());

        return "customer/domain_summary";
    }

}
