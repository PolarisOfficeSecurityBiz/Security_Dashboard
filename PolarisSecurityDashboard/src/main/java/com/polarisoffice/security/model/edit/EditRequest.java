// src/main/java/com/polarisoffice/security/model/edit/EditRequest.java
package com.polarisoffice.security.model.edit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "edit_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EditRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 고객사 식별자 (필수) */
    @Column(nullable = false, length = 64)
    private String customerId;

    /** 서비스 식별자 (서비스 수정요청일 때만 사용) */
    private Integer serviceId;

    /** 요청 대상 타입 (COMPANY/SERVICE) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EditTargetType targetType;

    /** 요청 본문 (필수) */
    @Column(nullable = false, length = 2000)
    private String content;

    /** 요청자(담당자) 표시용 정보 */
    @Column(length = 100)
    private String requesterName;

    @Column(length = 200)
    private String requesterEmail;

    /** 상태 (기본 PENDING) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EditRequestStatus status = EditRequestStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    /** 처리 정보(관리자) */
    private LocalDateTime handledAt;

    @Column(length = 100)
    private String handledBy; // admin email or name

    @Column(length = 1000)
    private String adminMemo;
}
