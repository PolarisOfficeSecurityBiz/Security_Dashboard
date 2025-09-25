// model/LogEntry.java
package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licenseKey;
    private String domain;

    @Enumerated(EnumType.STRING)
    private LogType logType;

    private LocalDateTime createdAt;

    // DTO는 문자열이지만, 여기서는 Integer로 유지 → 서비스에서 파싱
    private Integer osVersion;
    private Integer appVersion;

    // DTO.extra(Map) → 여기서는 JSON 문자열로 저장
    @Column(columnDefinition = "TEXT")
    private String extra;
}
