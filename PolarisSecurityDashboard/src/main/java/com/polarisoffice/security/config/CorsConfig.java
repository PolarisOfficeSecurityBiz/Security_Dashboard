package com.polarisoffice.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * 전역 CORS 설정.
 * - 프런트가 여러 도메인이면 setAllowedOrigins 대신 setAllowedOriginPatterns 사용.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 정확한 프런트 오리진을 넣으세요. (예: https://admin.example.com)
        cfg.setAllowedOrigins(List.of(
            "http://localhost:8080",     // 로컬 개발
            "http://localhost:3000",
            "http://61.96.206.88:80"  // 운영 프런트
        ));

        // 만약 서브도메인 와일드카드가 필요하면 아래를 사용(그리고 setAllowedOrigins는 제거)
        // cfg.setAllowedOriginPatterns(List.of("https://*.your-frontend.com"));

        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Content-Type", "Accept", "Authorization", "X-Requested-With", "X-XSRF-TOKEN"));

        // 인증정보(쿠키/Authorization) 전달 허용 → AllowedOrigins에 '*' 사용 금지
        cfg.setAllowCredentials(true);

        // 프리플라이트 캐시(초)
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}