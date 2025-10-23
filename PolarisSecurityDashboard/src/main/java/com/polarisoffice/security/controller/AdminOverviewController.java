package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.MetricsDto;
import com.polarisoffice.security.dto.RecentCustomerRow;
import com.polarisoffice.security.model.CustomerService;
import com.polarisoffice.security.model.edit.EditRequest;
import com.polarisoffice.security.model.edit.EditRequestStatus;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.CompanyServiceRepository;  // 수정된 리포지토리
import com.polarisoffice.security.service.CompanyServiceService;
import com.polarisoffice.security.service.EditRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class AdminOverviewController {

    private final CustomerRepository customerRepository;
    private final CompanyServiceRepository companyServiceRepository;  // 수정된 리포지토리
    private final EditRequestService editRequestService;
    private final CompanyServiceService companyService;

    @GetMapping("/admin/overview")
    public String overview(Model model) {

        // KPI: 고객사 총 수와 서비스별 고객사 수 (중복 제거)
        long totalCustomers = customerRepository.count();
        long vguardCount  = companyServiceRepository.countDistinctCustomerByServiceName("V-Guard");
        long secuoneCount = companyServiceRepository.countDistinctCustomerByServiceName("SecuOne");

        MetricsDto metrics = new MetricsDto(totalCustomers, vguardCount, secuoneCount);

        // 최근 수정 요청
        var latest = editRequestService.getLatestTop20();
        List<Map<String, Object>> reqView = new ArrayList<>();
        long pending = 0L, inProgress = 0L, resolved = 0L;

        for (EditRequest r : latest) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());

            String formattedDate = (r.getCreateAt() != null)
                    ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                      .format(r.getCreateAt().atZone(ZoneOffset.UTC).toLocalDateTime())
                    : "No Date Available";
            m.put("createdAt", formattedDate);

            m.put("requester", firstNonNull(r.getRequesterName(), r.getRequesterEmail(), "-"));
            m.put("requesterEmail", r.getRequesterEmail());

            String type = r.getTargetType() != null ? r.getTargetType().name() : "-";
            m.put("type", type);
            m.put("target", "SERVICE".equals(type)
                    ? "서비스 #" + (r.getServiceId() != null ? r.getServiceId() : 0)
                    : "회사 정보");

            m.put("customerId", r.getCustomerId());
            m.put("serviceId", r.getServiceId());

            EditRequestStatus status = Optional.ofNullable(r.getStatus())
                    .orElse(EditRequestStatus.PENDING);
            m.put("status", status.name());
            m.put("snippet", summarize(r.getContent(), 120));

            reqView.add(m);

            switch (status) {
                case PENDING -> pending++;
                case IN_PROGRESS -> inProgress++;
                case RESOLVED -> resolved++;
            }
        }

        Map<String, Long> reqCounts = Map.of(
                "pending", pending,
                "in_progress", inProgress,
                "resolved", resolved
        );

        // 최근 3개월 등록 고객사
        List<RecentCustomerRow> recent = companyService.getRecentCustomers3Months();

        // 모델 바인딩
        model.addAttribute("metrics", metrics);
        model.addAttribute("requests", reqView);
        model.addAttribute("reqCounts", reqCounts);
        model.addAttribute("recentCustomers", recent);

        return "admin/overview";
    }

    private static String summarize(String s, int max) {
        if (s == null) return "";
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() > max ? t.substring(0, max) + "…" : t;
    }

    private static String firstNonNull(String a, String b, String fallback) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return fallback;
    }
}
