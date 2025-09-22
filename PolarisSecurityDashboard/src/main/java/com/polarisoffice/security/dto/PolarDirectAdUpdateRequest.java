package com.polarisoffice.security.dto;

import com.polarisoffice.security.model.AdType;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PolarDirectAdUpdateRequest {
private AdType adType;
private String advertiserName;
private String backgroundColor;
private String imageUrl;
private String targetUrl;
private Long clickCount;
private Long viewCount;
}