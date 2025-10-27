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
@RequestMapping("/api/log")
public class SecuOneLogController {

    private final SecuOneLogService secuOneLogService;
    private final SecuOneLogEventRepository secuOneLogRepository;

    /** 사용자 유입 로그 */
    @PostMapping("/acquisition")
    @ResponseBody
    public IdResponse logAcquisition(@Valid @RequestBody AcquisitionLogRequest reqDto,
                                     HttpServletRequest request) {
        Long id = secuOneLogService.logAcquisition(reqDto, request);
        return new IdResponse(id);
    }

    /** 주요 기능 클릭 로그 */
    @PostMapping("/feature-click")
    @ResponseBody
    public IdResponse logFeatureClick(@Valid @RequestBody FeatureClickLogRequest reqDto,
                                      HttpServletRequest request) {
        Long id = secuOneLogService.logFeatureClick(reqDto, request);
        return new IdResponse(id);
    }

    /** 관리자용 로그 화면 */
    @GetMapping("/admin/secuone/logs")
    public String showLogs(Model model) {
        model.addAttribute("logs", secuOneLogRepository.findAll(Sort.by(Sort.Direction.DESC, "eventTime")));
        return "admin/secuone/logs"; // templates/admin/secuone/logs.html
    }

    /** 로그 목록 API (AJAX용) */
    @GetMapping("/events")
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

    /** 내부용 ID 응답 */
    private record IdResponse(Long id) {}
}
