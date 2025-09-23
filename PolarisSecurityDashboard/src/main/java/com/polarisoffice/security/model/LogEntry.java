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
    private Integer osVersion;
    private Integer appVersion;

    @Column(columnDefinition = "TEXT")
    private String extra;
}
