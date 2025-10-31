package com.polarisoffice.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarisoffice.security.dto.LogListItem;
import com.polarisoffice.security.dto.LogUpsertRequest;
import com.polarisoffice.security.model.*;
import com.polarisoffice.security.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogEntryRepository logEntryRepo;
    private final MalwareLogRepository malwareRepo;
    private final RemoteLogRepository remoteRepo;
    private final RootingLogRepository rootingRepo;

    private final ObjectMapper om = new ObjectMapper();

    /* -----------------------------
       ✅ 로그 생성
    ------------------------------ */
    @Transactional
    public Long createLogWithDetail(LogUpsertRequest req) {
        LogEntry entry = LogEntry.builder()
                .licenseKey(req.licenseKey())
                .domain(req.domain())
                .logType(req.logType())
                .createdAt(LocalDateTime.now())
                .osVersion(parseIntOrNull(req.osVersion()))
                .appVersion(parseIntOrNull(req.appVersion()))
                .extra(toJsonOrNull(req.extra()))
                .build();
        logEntryRepo.save(entry);

        switch (req.logType()) {
            case MALWARE -> {
                var m = Objects.requireNonNull(req.malware(), "malware payload required");
                MalwareLog ml = MalwareLog.builder()
                        .logEntry(entry)
                        .malwareType(toJsonArray(m.malwareType()))
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
                        .remoteType(toJsonArray(r.remoteType()))
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

    /* -----------------------------
       ✅ 대시보드용 데이터
    ------------------------------ */

    /** 이번 달 유입 유저 수 */
    public int countMonthlyJoin(String domain) {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        return logEntryRepo.countByDomainAndCreatedAtBetween(domain, start, end);
    }

    /** 올해 월별 유입 현황 (차트용) */
    public Map<String, Integer> getYearlyJoinCount(String domain) {
        int year = Year.now().getValue();
        List<Object[]> rows = logEntryRepo.countMonthlyJoinByYear(domain, year);

        // {1월=200, 2월=150, ...} 형태로 변환
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            map.put(m + "월", 0);
        }
        for (Object[] r : rows) {
            int month = ((Number) r[0]).intValue();
            int count = ((Number) r[1]).intValue();
            map.put(month + "월", count);
        }
        return map;
    }

    /* -----------------------------
       ✅ 기존 로그 조회 기능
    ------------------------------ */
    public List<LogListItem> getReport(Integer days, LogType type, String domain) {
        int d = (days == null ? 7 : Math.max(1, Math.min(days, 30)));
        LocalDateTime from = LocalDateTime.now().minusDays(d);

        String domainLike = (domain == null || domain.isBlank()) ? null : domain;
        List<LogEntry> rows = logEntryRepo.findRecent(from, type, domainLike);

        return rows.stream()
                .map(l -> new LogListItem(
                        l.getId(),
                        l.getDomain(),
                        l.getLogType(),
                        l.getCreatedAt(),
                        l.getOsVersion(),
                        l.getAppVersion(),
                        l.getExtra()
                ))
                .toList();
    }

    public List<LogListItem> getLogsByExactDomain(String domain) {
        return logEntryRepo.findAll().stream()
                .filter(log -> log.getDomain() != null &&
                               log.getDomain().equalsIgnoreCase(domain))
                .map(LogListItem::fromEntity)
                .toList();
    }

    /* -----------------------------
       ✅ 내부 유틸
    ------------------------------ */
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
