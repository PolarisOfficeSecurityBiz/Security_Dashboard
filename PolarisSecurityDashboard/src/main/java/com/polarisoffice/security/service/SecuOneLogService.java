package com.polarisoffice.security.service;

import com.polarisoffice.security.model.SecuOneLogEvent;
import com.polarisoffice.security.repository.SecuOneLogEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecuOneLogService {

    private final SecuOneLogEventRepository logEventRepository;

    @Transactional
    public Long logAcquisition(com.polarisoffice.security.dto.AcquisitionLogRequest dto, HttpServletRequest req) {
        SecuOneLogEvent event = SecuOneLogEvent.builder()
                .eventType("acquisition")
                .userId(dto.getUserId())
                .sessionId(dto.getSessionId())
                .ip(extractClientIp(req))
                .userAgent(req.getHeader("User-Agent"))
                .acqChannel(nullIfBlank(dto.getChannel()))
                .utmSource(nullIfBlank(dto.getUtmSource()))
                .utmMedium(nullIfBlank(dto.getUtmMedium()))
                .utmCampaign(nullIfBlank(dto.getUtmCampaign()))
                .referrer(nullIfBlank(dto.getReferrer()))
                .build();

        SecuOneLogEvent saved = logEventRepository.save(event);
        log.info("[ACQ] channel={}, utm=({},{},{}), ref={}, user={}, sid={}",
                event.getAcqChannel(), event.getUtmSource(), event.getUtmMedium(),
                event.getUtmCampaign(), event.getReferrer(), event.getUserId(), event.getSessionId());
        return saved.getId();
    }

    @Transactional
    public Long logFeatureClick(com.polarisoffice.security.dto.FeatureClickLogRequest dto, HttpServletRequest req) {
    	SecuOneLogEvent event = SecuOneLogEvent.builder()
                .eventType("feature_click")
                .userId(dto.getUserId())
                .sessionId(dto.getSessionId())
                .ip(extractClientIp(req))
                .userAgent(req.getHeader("User-Agent"))
                .featureName(nullIfBlank(dto.getFeatureName()))
                .featureLabel(nullIfBlank(dto.getFeatureLabel()))
                .extra(nullIfBlank(dto.getExtra()))
                .build();

    	SecuOneLogEvent saved = logEventRepository.save(event);
        log.info("[CLICK] feature={}, label={}, user={}, sid={}, extra={}",
                event.getFeatureName(), event.getFeatureLabel(),
                event.getUserId(), event.getSessionId(), event.getExtra());
        return saved.getId();
    }

    private String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // 첫 번째가 실제 클라이언트 IP
            return xff.split(",")[0].trim();
        }
        String ip = req.getHeader("X-Real-IP");
        return (ip != null && !ip.isBlank()) ? ip : req.getRemoteAddr();
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
