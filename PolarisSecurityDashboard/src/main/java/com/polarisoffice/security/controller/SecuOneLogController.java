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
     * utmSource가 'com.polarisoffice.vguardsecuone' 이 아닌 로그만 조회
     * → 즉, 외부(고객사) 유입 로그
     */
    @GetMapping("/customer/logs")
    public String showCustomerLogs(Model model) {

        // ✅ 로그 데이터 조회
        List<SecuOneLogEvent> customerLogs = secuOneLogRepository.findAll().stream()
                .filter(e -> e.getUtmSource() != null &&
                             !e.getUtmSource().equalsIgnoreCase("com.polarisoffice.vguardsecuone"))
                .sorted(Comparator.comparing(SecuOneLogEvent::getEventTime).reversed())
                .collect(Collectors.toList());

        // ✅ 도메인 하드코딩 or 동적 지정
        model.addAttribute("domain", "m.yebyeol.co.kr");

        // ✅ 로그 리스트 전달
        model.addAttribute("logs", customerLogs);

        // ✅ 디버깅용 로그
        System.out.println("총 고객 로그 수: " + customerLogs.size());

        return "customer/customer_logs";
    }

    /**
     * 고객 로그 JSON API (AJAX)
     * - 특정 utmSource(도메인) 기반 필터링 가능
     * - 날짜 필터는 추후 확장 용도
     */
    @GetMapping("/customer/logs/api")
    @ResponseBody
    public List<SecuOneLogEvent> getCustomerLogs(
            @RequestParam(required = false) String utmSource,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        return secuOneLogRepository.findAll().stream()
                .filter(e -> e.getUtmSource() != null &&
                             !e.getUtmSource().equalsIgnoreCase("com.polarisoffice.vguardsecuone"))
                .filter(e -> (utmSource == null || e.getUtmSource().equalsIgnoreCase(utmSource)))
                .sorted(Comparator.comparing(SecuOneLogEvent::getEventTime).reversed())
                .collect(Collectors.toList());
    }

    /* 내부 응답 DTO */
    private record IdResponse(Long id) {}
}
