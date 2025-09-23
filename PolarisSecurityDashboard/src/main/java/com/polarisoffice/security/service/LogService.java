package com.polarisoffice.security.service;

import com.polarisoffice.security.dto.*;
import com.polarisoffice.security.model.*;
import com.polarisoffice.security.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogEntryRepository logEntryRepo;
    private final MalwareLogRepository malwareRepo;
    private final RemoteLogRepository remoteRepo;
    private final RootingLogRepository rootingRepo;

    public LogEntry createLog(LogEntryRequest req) {
        LogEntry entry = LogEntry.builder()
                .licenseKey(req.getLicenseKey())
                .domain(req.getDomain())
                .logType(req.getLogType())
                .createdAt(LocalDateTime.now())
                .osVersion(req.getOsVersion())
                .appVersion(req.getAppVersion())
                .extra(req.getExtra())
                .build();
        return logEntryRepo.save(entry);
    }

    public MalwareLog attachMalware(MalwareLogRequest req) {
        LogEntry entry = logEntryRepo.findById(req.getLogEntryId()).orElseThrow();
        MalwareLog ml = MalwareLog.builder()
                .logEntry(entry)
                .malwareType(req.getMalwareType())
                .packageName(req.getPackageName())
                .path(req.getPath())
                .androidId(req.getAndroidId())
                .build();
        return malwareRepo.save(ml);
    }

    // Remote, Rooting 도 같은 방식
}
