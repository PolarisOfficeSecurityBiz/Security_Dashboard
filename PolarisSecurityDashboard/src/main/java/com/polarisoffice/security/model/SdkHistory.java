package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sdk_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SdkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 반드시 Sdk와 연결되어야 함!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sdk_id", nullable = false)
    private Sdk sdk;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = false)
    private String uploadedBy;

    @Column(length = 255)
    private String releaseNote;

    @Column(length = 255)
    private String downloadUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
