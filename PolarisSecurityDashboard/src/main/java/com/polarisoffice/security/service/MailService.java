// src/main/java/com/polarisoffice/security/service/MailService.java
package com.polarisoffice.security.service;

import com.polarisoffice.security.model.edit.EditRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@example.com}")
    private String from;

    @Value("${app.portal.url:https://example.com}")
    private String portalUrl;

    /** 기존: 초기 비밀번호 안내 */
    public void sendInitialPassword(String to, String name, String tempPassword) {
        if (to == null || to.isBlank()) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("[Polaris Security] 초기 비밀번호 안내");
        msg.setText("""
                안녕하세요 %s 님,

                Polaris Security 포털 계정이 생성되었습니다.
                임시 비밀번호: %s

                아래 링크에서 로그인 후 즉시 비밀번호를 변경해 주세요.
                %s

                감사합니다.
                """.formatted(name, tempPassword, portalUrl));

        mailSender.send(msg);
    }

    /** 추가: 수정요청 '완료' 알림 */
    public void sendEditRequestResolved(String to, String name, EditRequest req) {
        if (to == null || to.isBlank() || req == null) return;

        String displayName = (name == null || name.isBlank()) ? "" : " " + name;
        String subject = "[Polaris Security] 요청하신 수정사항이 처리 완료되었습니다.";

        String serviceLine = (req.getServiceId() != null)
                ? "\n서비스 ID: " + req.getServiceId()
                : "";

        String text = """
                안녕하세요%s 님,

                요청하신 수정사항이 완료되었습니다.

                요청 ID: %d
                구분: %s
                고객사 ID: %s%s
                요청 내용(요약): %s

                (참고) 포털: %s

                감사합니다.
                """.formatted(
                displayName,
                req.getId(),
                String.valueOf(req.getTargetType()),
                req.getCustomerId() == null ? "-" : req.getCustomerId(),
                serviceLine,
                summarize(req.getContent(), 200),
                portalUrl
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);

        mailSender.send(msg);
    }

    private static String summarize(String s, int max) {
        if (s == null) return "";
        String t = s.trim().replaceAll("\\s+", " ");
        return t.length() > max ? t.substring(0, max) + "…" : t;
    }
}
