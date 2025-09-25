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

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

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
    
}
