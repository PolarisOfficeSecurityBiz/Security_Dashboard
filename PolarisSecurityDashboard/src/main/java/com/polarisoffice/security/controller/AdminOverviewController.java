package com.polarisoffice.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

/**
 * 관리자 대시보드 개요 페이지 컨트롤러
 * URL: /admin/overview
 * View: templates/admin/overview.html
 */
@Controller
public class AdminOverviewController {

    @GetMapping("/admin/overview")
    public String overview(Model model) {

        // ✅ 1. 상단 메트릭 (KPI 카드)
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCustomers", 14);
        metrics.put("vguard", 6);
        metrics.put("secuone", 8);
        model.addAttribute("metrics", metrics);

        // ✅ 2. 로그 통계 (최근 7일 그래프 + 도메인별 로그)
        Map<String, Object> logsSummary = new HashMap<>();
        logsSummary.put("total", 68);
        logsSummary.put("malware", 12);
        logsSummary.put("remote", 5);
        logsSummary.put("rooting", 4);
        logsSummary.put("byDayCsv", "3,5,8,10,12,18,12");

        List<Map<String, Object>> byDomain = new ArrayList<>();
        byDomain.add(Map.of("domain", "corp.polaris.com", "count", 23));
        byDomain.add(Map.of("domain", "secure.demo.net", "count", 18));
        byDomain.add(Map.of("domain", "alpha.client.kr", "count", 12));
        logsSummary.put("byDomain", byDomain);

        model.addAttribute("logsSummary", logsSummary);

        // ✅ 3. 최근 등록 고객사
        List<Map<String, Object>> recentCustomers = new ArrayList<>();
        recentCustomers.add(Map.of("customerName", "폴라리스테스트", "connectedCompany", "PolarisSoft", "createAt", "2025-09-16"));
        recentCustomers.add(Map.of("customerName", "비전시큐리티", "connectedCompany", "VisionSecu", "createAt", "2025-09-18"));
        recentCustomers.add(Map.of("customerName", "알파테크", "connectedCompany", "AlphaTech", "createAt", "2025-10-01"));
        model.addAttribute("recentCustomers", recentCustomers);

        // ✅ 4. 최근 보안 로그
        List<Map<String, Object>> recentLogs = new ArrayList<>();
        recentLogs.add(Map.of(
                "id", 1,
                "createdAt", "2025-09-25 11:39",
                "domain", "kr.co.sample.vguard2",
                "logType", "MALWARE",
                "osVersion", "16",
                "appVersion", "2.0"
        ));
        recentLogs.add(Map.of(
                "id", 2,
                "createdAt", "2025-09-25 11:45",
                "domain", "kr.co.demo.app",
                "logType", "REMOTE",
                "osVersion", "15",
                "appVersion", "3.1"
        ));
        recentLogs.add(Map.of(
                "id", 3,
                "createdAt", "2025-09-26 09:21",
                "domain", "secure.polaris.co.kr",
                "logType", "ROOTING",
                "osVersion", "17",
                "appVersion", "3.3"
        ));
        model.addAttribute("recentLogs", recentLogs);

        return "admin/overview"; // ✅ templates/admin/overview.html
    }
}