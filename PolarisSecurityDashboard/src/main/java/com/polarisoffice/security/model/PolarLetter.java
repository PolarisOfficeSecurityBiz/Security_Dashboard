package com.polarisoffice.security.model;

import com.google.cloud.firestore.annotation.PropertyName;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PolarLetter {
    private String id;          // Firestore doc id (수동 세팅)
    private String title;       // 제목
    private String author;      // 작성자
    private String content;     // 본문(요약문)
    private String thumbnail;   // 썸네일 URL
    private String url;         // 원문 링크
    private String updatedAt;   // 수정시각(ISO-8601) - 없으면 null이어도 OK

    /** Firestore 필드명 create_time 매핑 */
    private String createTime;  // "yyyy.MM.dd"

    @PropertyName("create_time")
    public String getCreateTime() { return createTime; }

    @PropertyName("create_time")
    public void setCreateTime(String v) { this.createTime = v; }
}
