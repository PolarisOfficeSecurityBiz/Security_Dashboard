package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.LogListItem;
import com.polarisoffice.security.model.SecuOneLogEvent;
import com.polarisoffice.security.repository.SecuOneLogEventRepository;
import com.polarisoffice.security.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 도메인별 요약 페이지:
 * - 시큐원 유입 사용자 수
 * - 해당 도메인으로 수집된 LogEntry(보안 로그) 목록
 */
@Controller
@RequiredArgsConstructor
public class DomainLogSummaryController {

    private final SecuOneLogEventRepository secuOneRepo;
    private final LogService logService;

    @GetMapping("/customer/domain-summary")
    public String showDomainSummary(
            @RequestParam(required = false, defaultValue = "com.polarisoffice.vguardsecuone") String domain,
            Model model
    ) {
        // ✅ 1️⃣ SecuOne 유입 로그 (utmSource 기준)
        List<SecuOneLogEvent> secuoneLogs = secuOneRepo.findAll().stream()
                .filter(e -> e.getUtmSource() != null)
                .filter(e -> e.getUtmSource().equalsIgnoreCase(domain))
                .collect(Collectors.toList());

        long userCount = secuoneLogs.size();

        // ✅ 2️⃣ LogEntry (보안 로그)
        List<LogListItem> logEntries = logService.getLogsByExactDomain(domain).stream()
                .sorted(Comparator.comparing(LogListItem::createdAt).reversed())
                .collect(Collectors.toList());

        // ✅ 3️⃣ 모델에 담기
        model.addAttribute("domain", domain);
        model.addAttribute("userCount", userCount);
        model.addAttribute("logEntries", logEntries);

        System.out.printf("✅ [Domain Summary] domain=%s | 유입:%d | 보안로그:%d%n",
                domain, userCount, logEntries.size());

        return "customer/domain_summary";
    }
}
