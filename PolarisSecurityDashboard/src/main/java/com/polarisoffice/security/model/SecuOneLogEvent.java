package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "secuone_log_event", indexes = {
        @Index(name = "idx_event_type_time", columnList = "eventType, eventTime"),
        @Index(name = "idx_user_feature_time", columnList = "userId, featureName, eventTime"),
        @Index(name = "idx_channel_time", columnList = "acqChannel, eventTime")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class SecuOneLogEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공통
    @CreationTimestamp
    private Instant eventTime;

    private String eventType;      // acquisition | feature_click

    private String userId;         // 익명/로그인 모두 수용 (ex. "anon:session-xxx" or "u123")
    private String sessionId;      // 클라이언트가 전달

    private String ip;
    private String userAgent;

    // 유입 경로
    private String acqChannel;     // push | email | seo | direct | referrer | campaign 등
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String referrer;

    // 기능 클릭
    private String featureName;    // URL_SCAN | REALTIME_SMISHING | LOGIN | SIGNUP 등
    private String featureLabel;   // 버튼 라벨/화면명 등 세부 식별
    private String extra;          // JSON or key=value 콤마 구분(간단용)
}