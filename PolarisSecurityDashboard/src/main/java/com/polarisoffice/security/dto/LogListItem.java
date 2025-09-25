package com.polarisoffice.security.dto;

import com.polarisoffice.security.model.LogType;
import java.time.LocalDateTime;

public record LogListItem(
        Long id,
        String domain,
        LogType logType,
        LocalDateTime createdAt,
        Integer osVersion,
        Integer appVersion,
        String extra // DB에 TEXT(String)로 저장되어 있으면 그대로 반환
) {}