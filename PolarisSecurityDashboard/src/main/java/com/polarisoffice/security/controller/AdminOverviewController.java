package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.MetricsDto;
import com.polarisoffice.security.model.edit.EditRequest;
import com.polarisoffice.security.model.edit.EditRequestStatus;
import com.polarisoffice.security.model.edit.EditTargetType;
import com.polarisoffice.security.repository.CustomerRepository;
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
    private final EditRequestService editRequestService;

    @GetMapping("/admin/overview")
    public String overview(Model model) {

        var latest = editRequestService.getLatestTop20();
        List<Map<String, Object>> reqView = new ArrayList<>();

        // KPI (예시 값)
        long totalCustomers = customerRepository.count();
        MetricsDto metrics = new MetricsDto(totalCustomers, 100, 50);

        long pending = 0L, inProgress = 0L, resolved = 0L;

        for (EditRequest r : latest) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());

            // createdAt -> 문자열
            String formattedDate;
            if (r.getCreateAt() != null) {
                try {
                    formattedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            .format(r.getCreateAt().atZone(ZoneOffset.UTC).toLocalDateTime());
                } catch (Exception e) {
                    formattedDate = "Invalid Date";
                }
            } else {
                formattedDate = "No Date Available";
            }
            m.put("createdAt", formattedDate);

            // 요청자
            m.put("requester", firstNonNull(r.getRequesterName(), r.getRequesterEmail(), "-"));
            m.put("requesterEmail", r.getRequesterEmail());

            // 타입/타겟
            String type = r.getTargetType() != null ? r.getTargetType().name() : "-";
            m.put("type", type);
            m.put("target", "SERVICE".equals(type)
                    ? "서비스 #" + (r.getServiceId() != null ? r.getServiceId() : 0)
                    : "회사 정보");

            // ✅ 여기 추가: 링크에 쓸 식별자들
            m.put("customerId", r.getCustomerId());      // <-- 고객사 상세로 갈 때 사용
            m.put("serviceId", r.getServiceId());        // (필요시 서비스 상세 링크에도 사용 가능)

            // 상태/내용
            EditRequestStatus status = Optional.ofNullable(r.getStatus())
                    .orElse(EditRequestStatus.PENDING);
            m.put("status", status.name());
            m.put("snippet", summarize(r.getContent(), 120));

            reqView.add(m);

            // 카운트
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

        model.addAttribute("metrics", metrics);
        model.addAttribute("requests", reqView);
        model.addAttribute("reqCounts", reqCounts);

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
