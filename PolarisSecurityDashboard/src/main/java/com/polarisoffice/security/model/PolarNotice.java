package com.polarisoffice.security.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor @Builder 
public class PolarNotice {
    private String id;  // Firestore의 document ID
    private String author;  // 작성자
    private String category;  // 카테고리 (예: 이벤트, 보안뉴스 등)
    private String content;  // 내용
    private String date;  // 작성 날짜
    private String imageURL;  // 이미지 URL
    private String title;  // 제목
}