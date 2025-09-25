package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.*;
import com.polarisoffice.security.model.*;
import com.polarisoffice.security.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Logs API", description = "로그 수집 및 분석 관련 API")
public class LogController {
    private final LogService logService;
    @PostMapping("/v2")
    @Operation(
        summary = "로그 생성(원샷)",
        description = "공통 로그(LogEntry)와 상세 로그(Malware/Remote/Rooting)를 한 요청으로 저장합니다. 바디 없이 201을 반환합니다."
    )
    public ResponseEntity<Void> createLogV2(@Valid @RequestBody LogUpsertRequest req) {
        Long id = logService.createLogWithDetail(req);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()               // /api/logs/v2
                .replacePath("/api/logs/{id}")      // 리소스의 표준 경로로 교체 (원하면 바꿔도 됨)
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();  // 201 + Location 헤더, 바디 없음
    }
}
