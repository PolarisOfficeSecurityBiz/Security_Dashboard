// src/main/java/com/polarisoffice/security/model/LicenseHistory.java
package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "license_history")
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")                 // pk
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id", nullable = false)  // fk → license.license_id
    private License license;

    @Column(name = "user_id", length = 100) // fk(사용자 테이블이 있다면 실제 FK로 매핑 가능)
    private String userId;

    @Column(name = "commit_message", length = 500) // "commit Message" → commit_message 로 저장
    private String commitMessage;

    @Column(name = "create_at", nullable = false)  // 생성날짜
    private LocalDateTime createAt;

    @PrePersist
    void onCreate() {
        if (createAt == null) createAt = LocalDateTime.now();
    }
}
