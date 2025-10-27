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

    /** 로그 페이지 (Thymeleaf HTML) */
    @GetMapping("/admin/secuone/logs")
    public String showLogs(Model model) {
        model.addAttribute("logs",
                secuOneLogRepository.findAll(Sort.by(Sort.Direction.DESC, "eventTime")));
        return "admin/secuone/logs";  // templates/admin/secuone/logs.html
    }

    /** 로그 데이터 (AJAX용 JSON) */
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
                        (e.getAcqChannel() != null && e.getAcqChannel().contains(keyword))))
                .sorted(Comparator.comparing(SecuOneLogEvent::getEventTime).reversed())
                .toList();
    }

    /* 내부용 응답 DTO */
    private record IdResponse(Long id) {}
}
