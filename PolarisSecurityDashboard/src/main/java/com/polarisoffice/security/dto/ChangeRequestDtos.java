// src/main/java/com/polarisoffice/security/dto/ChangeRequestDtos.java
package com.polarisoffice.security.dto;

import com.polarisoffice.security.model.ChangeRequest.RequestTarget;
import lombok.*;

import java.time.LocalDateTime;

public class ChangeRequestDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateReq {
        private String customerId;   // 필수
        private Integer serviceId;   // SERVICE일 때만
        private RequestTarget target; // COMPANY or SERVICE
        private String title;        // 요약
        private String detailsJson;  // 변경 상세(JSON 문자열)
        // 클라이언트에서 보낼 수 있는 요청자 표시
        private String requesterName;
        private String requesterEmail;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemRes {
        private Long id;
        private String customerId;
        private Integer serviceId;
        private RequestTarget target;
        private String title;
        private String detailsJson;
        private String status;
        private String requesterName;
        private String requesterEmail;
        private LocalDateTime createdAt;
        private LocalDateTime reviewedAt;
        private String adminComment;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReviewReq {
        private String adminComment; // 승인/반려 코멘트
    }
}
