// com.polarisoffice.security.dto.LogUpsertRequest.java
package com.polarisoffice.security.dto;

import com.polarisoffice.security.model.LogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record LogUpsertRequest(
        @NotBlank String licenseKey,
        @NotBlank String domain,
        @NotNull  LogType logType,
        String osVersion,
        String appVersion,
        Map<String, Object> extra,   // 자유 확장 필드 (null 가능)
        MalwarePart malware,         // logType=MALWARE 일 때 필요
        RemotePart remote,           // logType=REMOTE  일 때 필요
        RootingPart rooting          // logType=ROOTING 일 때 필요
) {
    // 악성코드 상세
    public record MalwarePart(
            @NotNull List<String> malwareType,
            @NotBlank String packageName,
            @NotBlank String path,
            @NotBlank String androidId
    ) {}
    // 원격앱 상세
    public record RemotePart(
            @NotNull List<String> remoteType,
            @NotBlank String packageName,
            @NotBlank String path,
            @NotBlank String androidId
    ) {}
    // 루팅 상세
    public record RootingPart(
            @NotBlank String rootReason,
            @NotBlank String path,
            @NotBlank String androidId
    ) {}
}
