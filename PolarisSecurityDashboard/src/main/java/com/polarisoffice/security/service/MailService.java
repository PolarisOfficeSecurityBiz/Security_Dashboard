// src/main/java/com/polarisoffice/security/service/MailService.java
package com.polarisoffice.security.service;

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
}
