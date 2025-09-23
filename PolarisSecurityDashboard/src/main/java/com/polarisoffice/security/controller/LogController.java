package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.*;
import com.polarisoffice.security.model.*;
import com.polarisoffice.security.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    private final LogService logService;

    @PostMapping
    public ResponseEntity<LogEntry> createLog(@Valid @RequestBody LogEntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logService.createLog(req));
    }

    @PostMapping("/malware")
    public ResponseEntity<MalwareLog> addMalware(@Valid @RequestBody MalwareLogRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(logService.attachMalware(req));
    }

    // /remote, /rooting 엔드포인트도 같은 방식
}
