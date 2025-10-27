package com.polarisoffice.security.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeatureClickLogRequest {
    @NotBlank
    private String userId;
    private String sessionId;

    @NotBlank
    private String featureName;   // 예: URL_SCAN, REALTIME_SMISHING
    private String featureLabel;  // 버튼 텍스트/화면명(선택)

    private String extra;         // {"screen":"home","ab":"B"} 같은 JSON 문자열 or key=value
}