package com.polarisoffice.security.dto;

import com.polarisoffice.security.model.LogEntry;
import com.polarisoffice.security.model.LogType;

import java.time.LocalDateTime;

/**
 * 로그 간략 정보 DTO
 * - LogEntry 엔티티에서 핵심 필드만 추출하여 반환
 */
public record LogListItem(
        Long id,
        String domain,
        LogType logType,
        LocalDateTime createdAt,
        Integer osVersion,
        Integer appVersion,
        String extra // DB에 TEXT(String)로 저장되어 있으면 그대로 반환
) {
    /** ✅ 엔티티 -> DTO 변환용 정적 메서드 */
    public static LogListItem fromEntity(LogEntry entity) {
        return new LogListItem(
                entity.getId(),
                entity.getDomain(),
                entity.getLogType(),
                entity.getCreatedAt(),
                entity.getOsVersion(),
                entity.getAppVersion(),
                entity.getExtra()
        );
    }
}
