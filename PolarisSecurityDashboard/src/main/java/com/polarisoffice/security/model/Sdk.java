package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "sdk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sdk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("SDK ID")
    private Long id;

    @Column(nullable = false, length = 100)
    @Comment("SDK 유형명")
    private String sdkType;

    @Column(nullable = false, length = 200)
    @Comment("파일명")
    private String fileName;

    @Column(nullable = false, length = 255)
    @Comment("파일 경로")
    private String filePath;

    @Column(nullable = false)
    @Comment("업로드 일시")
    private LocalDateTime uploadedAt;

    @Column(nullable = false, length = 100)
    @Comment("업로드한 사용자")
    private String uploadedBy;
}
