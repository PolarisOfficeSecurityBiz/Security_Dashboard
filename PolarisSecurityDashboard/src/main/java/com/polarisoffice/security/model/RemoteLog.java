// model/RemoteLog.java
package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RemoteLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String remoteType;

    private String packageName;
    private String path;
    private String androidId;

    @OneToOne @JoinColumn(name="log_id")
    private LogEntry logEntry;
}
