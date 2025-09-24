package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.*;
import com.polarisoffice.security.model.*;
import com.polarisoffice.security.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Logs API", description = "로그 수집 및 분석 관련 API")
public class LogController {
    private final LogService logService;

    @PostMapping
    @Operation(summary = "일반 로그 생성", description = "LogEntryRequest를 받아 새로운 로그를 생성하고 ID를 반환합니다.")
    public ResponseEntity<Long> createLog(@Valid @RequestBody LogEntryRequest req) {
        LogEntry savedLog = logService.createLog(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLog.getId());
    }

    @PostMapping("/malware")
    @Operation(summary = "악성코드 로그 추가", description = "기존 로그에 MalwareLog 데이터를 첨부합니다.")
    public ResponseEntity<MalwareLog> addMalware(@Valid @RequestBody MalwareLogRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logService.attachMalware(req));
    }

    @PostMapping("/remote")
    @Operation(summary = "원격 앱 로그 추가", description = "기존 로그에 RemoteLog 데이터를 첨부합니다.")
    public ResponseEntity<RemoteLog> addRemote(@Valid @RequestBody RemoteLogRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logService.attachRemote(req));
    }

    @PostMapping("/rooting")
    @Operation(summary = "루팅 로그 추가", description = "기존 로그에 RootingLog 데이터를 첨부합니다.")
    public ResponseEntity<RootingLog> addRooting(@Valid @RequestBody RootingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logService.attachRooting(req));
    }
}
