// model/RootingLog.java  ← 누락되어 있던 엔티티 추가
package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RootingLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rootReason;
    private String path;
    private String androidId;

    @OneToOne @JoinColumn(name="log_id", nullable = false, unique = true)
    private LogEntry logEntry;
}
