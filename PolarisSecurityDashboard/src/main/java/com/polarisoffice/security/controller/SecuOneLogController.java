package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.AcquisitionLogRequest;
import com.polarisoffice.security.dto.FeatureClickLogRequest;
import com.polarisoffice.security.model.SecuOneLogEvent;
import com.polarisoffice.security.repository.SecuOneLogEventRepository;
import com.polarisoffice.security.service.SecuOneLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SecuOneLogController {

    private final SecuOneLogService secuOneLogService;
    private final SecuOneLogEventRepository secuOneLogRepository;

    /* -------------------- 1️⃣ 앱 로그 수집 API -------------------- */

    /** 사용자 유입 로그 */
    @PostMapping("/api/log/acquisition")
    @ResponseBody
    public IdResponse logAcquisition(@Valid @RequestBody AcquisitionLogRequest reqDto,
                                     HttpServletRequest request) {
        Long id = secuOneLogService.logAcquisition(reqDto, request);
        return new IdResponse(id);
    }

    /** 주요 기능 클릭 로그 */
    @PostMapping("/api/log/feature-click")
    @ResponseBody
    public IdResponse logFeatureClick(@Valid @RequestBody FeatureClickLogRequest reqDto,
                                      HttpServletRequest request) {
        Long id = secuOneLogService.logFeatureClick(reqDto, request);
        return new IdResponse(id);
    }


    /* -------------------- 2️⃣ 관리자 화면 -------------------- */

    /** 관리자 로그 페이지 */
    @GetMapping("/admin/secuone/logs")
    public String showLogs(Model model) {
        model.addAttribute("logs",
                secuOneLogRepository.findAll(Sort.by(Sort.Direction.DESC, "eventTime")));
        return "admin/secuone/logs";  // templates/admin/secuone/logs.html
    }

    /** 관리자 로그 API */
    @GetMapping("/admin/secuone/logs/api")
    @ResponseBody
    public List<SecuOneLogEvent> getAllLogs(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String keyword
    ) {
        if (eventType == null && keyword == null)
            return secuOneLogRepository.findAll(Sort.by(Sort.Direction.DESC, "eventTime"));

        return secuOneLogRepository.findAll().stream()
                .filter(e -> (eventType == null || e.getEventType().equals(eventType)))
                .filter(e -> (keyword == null ||
                        (e.getUserId() != null && e.getUserId().contains(keyword)) ||
                        (e.getFeatureName() != null && e.getFeatureName().contains(keyword)) ||
                        (e.getAcqChannel() != null && e.getAcqChannel().contains(keyword)) ||
                        (e.getUtmSource() != null && e.getUtmSource().contains(keyword))))
                .sorted(Comparator.comparing(SecuOneLogEvent::getEventTime).reversed())
                .toList();
    }


    /* -------------------- 3️⃣ 고객 전용 로그 -------------------- */

    /**
     * 고객 로그 페이지
     * 기본 도메인: com.polarisoffice.vguardsecuone
     * 파라미터 ?domain=... 으로 utm_source 기준 필터링
     */
    @GetMapping("/customer/logs")
    public String showCustomerLogs(
            @RequestParam(required = false, defaultValue = "com.polarisoffice.vguardsecuone") String domain,
            Model model
    ) {
        // ✅ utmSource 기준으로 필터링
        List<SecuOneLogEvent> customerLogs = secuOneLogRepository.findAll().stream()
                .filter(e -> e.getUtmSource() != null)
                .filter(e -> e.getUtmSource().equalsIgnoreCase(domain))
                .sorted(Comparator.comparing(SecuOneLogEvent::getEventTime).reversed())
                .collect(Collectors.toList());

        model.addAttribute("domain", domain);
        model.addAttribute("logs", customerLogs);

        System.out.printf("✅ [유입 로그] domain=%s, count=%d%n", domain, customerLogs.size());
        return "customer/customer_logs";
    }

    /**
     * 고객 로그 JSON API (AJAX용)
     * - 특정 utmSource(도메인) 기반 필터링 가능
     * - 날짜 필터 확장 가능
     */
    @GetMapping("/customer/logs/api")
    @ResponseBody
    public List<SecuOneLogEvent> getCustomerLogsApi(
            @RequestParam(required = false, defaultValue = "com.polarisoffice.vguardsecuone") String domain,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return secuOneLogRepository.findAll().stream()
                .filter(e -> e.getUtmSource() != null)
                .filter(e -> e.getUtmSource().equalsIgnoreCase(domain))
                .sorted(Comparator.comparing(SecuOneLogEvent::getEventTime).reversed())
                .collect(Collectors.toList());
    }

    /* -------------------- 내부용 DTO -------------------- */
    private record IdResponse(Long id) {}
}
