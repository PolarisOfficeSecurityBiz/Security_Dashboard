// src/main/java/com/polarisoffice/security/model/ChangeRequest.java
package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "change_request")
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 고객사 (필수) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** 대상 서비스 (회사 정보 수정이면 null) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service; // nullable

    /** 요청 카테고리: COMPANY / SERVICE */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RequestTarget target;

    /** 처리 상태: PENDING/APPROVED/REJECTED/CANCELED */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RequestStatus status;

    /** 요청자 식별(로그인을 고객 담당자 계정으로 한다면 userId or email 등) */
    @Column(length = 120) private String requesterUserId;
    @Column(length = 120) private String requesterName;
    @Column(length = 200) private String requesterEmail;

    /** 변경 요약/제목 */
    @Column(length = 200) private String title;

    /** 변경 상세(자유형). JSON(text) 추천 */
    @Lob
    @Comment("요청 상세(JSON 또는 텍스트)")
    private String detailsJson;

    /** 관리자 코멘트(승인/반려 사유) */
    @Column(length = 500)
    private String adminComment;

    /** 관리자 처리자 */
    @Column(length = 120)
    private String reviewerUserId;

    /** 타임스탬프 */
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = RequestStatus.PENDING;
    }

    // 선택: enum 내부 정의
    public enum RequestTarget { COMPANY, SERVICE }
    public enum RequestStatus { PENDING, APPROVED, REJECTED, CANCELED }
}
