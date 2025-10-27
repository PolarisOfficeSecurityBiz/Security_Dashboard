package com.polarisoffice.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.polarisoffice.security.dto.AcquisitionLogRequest;
import com.polarisoffice.security.dto.FeatureClickLogRequest;
import com.polarisoffice.security.service.SecuOneLogService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
public class SecuOneLogController {

    private final SecuOneLogService secuOneLogService;

    // 사용자 유입 경로 로그
    @PostMapping("/acquisition")
    public ResponseEntity<?> logAcquisition(@Valid @RequestBody AcquisitionLogRequest reqDto,
                                            HttpServletRequest request) {
        Long id = secuOneLogService.logAcquisition(reqDto, request);
        return ResponseEntity.ok().body(new IdResponse(id));
    }

    // 주요 기능 클릭 로그
    @PostMapping("/feature-click")
    public ResponseEntity<?> logFeatureClick(@Valid @RequestBody FeatureClickLogRequest reqDto,
                                             HttpServletRequest request) {
        Long id = secuOneLogService.logFeatureClick(reqDto, request);
        return ResponseEntity.ok().body(new IdResponse(id));
    }

    // 간단한 응답 DTO
    private record IdResponse(Long id) {}
}
