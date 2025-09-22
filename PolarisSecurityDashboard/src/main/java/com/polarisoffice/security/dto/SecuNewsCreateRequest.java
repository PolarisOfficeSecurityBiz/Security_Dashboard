package com.polarisoffice.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SecuNewsCreateRequest {
    @NotBlank private String title;
    @NotBlank private String url;

    private String category;   // 기본값: "보안뉴스"
    private String thumbnail;  // 옵션
    private String date;       // 옵션: 미지정이면 오늘(Asia/Seoul)로 세팅
}
