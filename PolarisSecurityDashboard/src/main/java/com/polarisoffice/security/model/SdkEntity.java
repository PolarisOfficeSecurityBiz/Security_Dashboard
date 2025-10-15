package com.polarisoffice.security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sdk_history")
public class SdkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sdkType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private String uploadedBy;  // 로그인 계정 이름

    // 기본 생성자
    public SdkEntity() {}

    public SdkEntity(String sdkType, String fileName, String filePath, LocalDateTime uploadedAt, String uploadedBy) {
        this.sdkType = sdkType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }

    // getter/setter 생략 가능 (Lombok 쓰면 @Data)
    public Long getId() { return id; }
    public String getSdkType() { return sdkType; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }

    public void setSdkType(String sdkType) { this.sdkType = sdkType; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
