package com.polarisoffice.security.dto;

import com.polarisoffice.security.model.AdType;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PolarDirectAdCreateRequest {
@NotNull
private AdType adType;
@NotBlank
private String advertiserName;
private String backgroundColor = "#FFFFFF";
@NotBlank
private String imageUrl;
@NotBlank
private String targetUrl;
}