package com.polarisoffice.security.dto;

import com.google.cloud.Timestamp;
import com.polarisoffice.security.model.AdType;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PolarDirectAdResponse {
private String id;
private AdType adType;
private String advertiserName;
private String backgroundColor;
private Long clickCount;
private String imageUrl;
private Timestamp publishedDate;
private String targetUrl;
private Timestamp updateAt;
private Long viewCount;
}