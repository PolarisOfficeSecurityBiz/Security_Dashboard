package com.polarisoffice.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcquisitionLogRequest {
    @NotBlank
    private String userId;       // 익명 가능
    private String sessionId;

    @NotBlank
    private String channel;      // push/email/seo/direct/referrer/campaign 등

    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String referrer;     // document.referrer 전달
}