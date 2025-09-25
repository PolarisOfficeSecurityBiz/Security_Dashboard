// com.polarisoffice.security.service.LogService.java
package com.polarisoffice.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarisoffice.security.dto.LogUpsertRequest;
import com.polarisoffice.security.model.*;
import com.polarisoffice.security.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogEntryRepository logEntryRepo;
    private final MalwareLogRepository malwareRepo;
    private final RemoteLogRepository remoteRepo;
    private final RootingLogRepository rootingRepo;

    private final ObjectMapper om = new ObjectMapper();

    @Transactional
    public Long createLogWithDetail(LogUpsertRequest req) {
        LogEntry entry = LogEntry.builder()
                .licenseKey(req.licenseKey())
                .domain(req.domain())
                .logType(req.logType())
                .createdAt(LocalDateTime.now())
                .osVersion(parseIntOrNull(req.osVersion()))
                .appVersion(parseIntOrNull(req.appVersion()))
                .extra(toJsonOrNull(req.extra()))   // Map → JSON 저장
                .build();
        logEntryRepo.save(entry);

        switch (req.logType()) {
            case MALWARE -> {
                var m = Objects.requireNonNull(req.malware(), "malware payload required");
                MalwareLog ml = MalwareLog.builder()
                        .logEntry(entry)
                        .malwareType(toJsonArray(m.malwareType())) // List<String> → JSON 문자열
                        .packageName(m.packageName())
                        .path(m.path())
                        .androidId(m.androidId())
                        .build();
                malwareRepo.save(ml);
            }
            case REMOTE -> {
                var r = Objects.requireNonNull(req.remote(), "remote payload required");
                RemoteLog rl = RemoteLog.builder()
                        .logEntry(entry)
                        .remoteType(toJsonArray(r.remoteType()))   // List<String> → JSON 문자열
                        .packageName(r.packageName())
                        .path(r.path())
                        .androidId(r.androidId())
                        .build();
                remoteRepo.save(rl);
            }
            case ROOTING -> {
                var r = Objects.requireNonNull(req.rooting(), "rooting payload required");
                RootingLog rl = RootingLog.builder()
                        .logEntry(entry)
                        .rootReason(r.rootReason())
                        .path(r.path())
                        .androidId(r.androidId())
                        .build();
                rootingRepo.save(rl);
            }
            default -> throw new IllegalArgumentException("Unsupported logType: " + req.logType());
        }

        return entry.getId();
    }

    private Integer parseIntOrNull(String v) {
        if (v == null || v.isBlank()) return null;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return null; }
    }

    private String toJsonOrNull(Map<String, Object> m) {
        if (m == null || m.isEmpty()) return null;
        try { return om.writeValueAsString(m); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("invalid extra json", e); }
    }

    private String toJsonArray(List<String> list) {
        if (list == null) return null;
        try { return om.writeValueAsString(list); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("invalid list json", e); }
    }
}
