package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.*;
import com.polarisoffice.security.model.*;
import com.polarisoffice.security.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Logs API", description = "로그 수집 및 분석 관련 API")
public class LogController {

    private final LogService logService;

    /* -------------------- 1️⃣ 로그 생성 -------------------- */
    @PostMapping("/v2")
    @Operation(
        summary = "로그 생성(원샷)",
        description = "공통 로그(LogEntry)와 상세 로그(Malware/Remote/Rooting)를 한 요청으로 저장합니다. 바디 없이 201을 반환합니다."
    )
    public ResponseEntity<Void> createLogV2(@Valid @RequestBody LogUpsertRequest req) {
        Long id = logService.createLogWithDetail(req);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()               // /api/logs/v2
                .replacePath("/api/logs/{id}")      // /api/logs/{id}
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    /* -------------------- 2️⃣ 리포트용 로그 조회 -------------------- */
    @GetMapping("/report")
    @Operation(summary = "최근 로그 조회(집계용 원본 목록)",
            description = """
                days 일수 이내의 로그 목록을 반환합니다(기본 7, 최대 30).
                선택 필터: type(MALWARE/REMOTE/ROOTING), domain(부분일치).
                응답은 프론트에서 클라이언트 집계/페이징에 사용됩니다.
                """)
    public ResponseEntity<List<LogListItem>> getReport(
            @Parameter(description = "조회 기간(일). 기본 7, 최대 30")
            @RequestParam(name = "days", required = false) Integer days,
            @Parameter(description = "로그 타입 필터 (예: MALWARE, REMOTE, ROOTING)")
            @RequestParam(name = "type", required = false) LogType type,
            @Parameter(description = "도메인 부분일치 필터")
            @RequestParam(name = "domain", required = false) String domain
    ) {
        return ResponseEntity.ok(logService.getReport(days, type, domain));
    }

    /* -------------------- 3️⃣ 도메인 일치 로그 조회 -------------------- */
    @GetMapping("/by-domain")
    @Operation(
        summary = "도메인별 로그 조회",
        description = """
            특정 도메인(utmSource 또는 domain 컬럼 기준)에 정확히 일치하는 로그만 반환합니다.  
            예시: /api/logs/by-domain?domain=m.yebyeol.co.kr
            """
    )
    public ResponseEntity<List<LogListItem>> getLogsByDomain(
            @Parameter(description = "정확히 일치시킬 도메인 (예: m.yebyeol.co.kr)", required = true)
            @RequestParam(name = "domain") String domain
    ) {
        List<LogListItem> logs = logService.getLogsByExactDomain(domain);
        return ResponseEntity.ok(logs);
    }
}
