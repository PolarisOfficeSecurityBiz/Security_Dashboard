package com.polarisoffice.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    // 개발 편의를 위해 두 오리진 허용 (운영에서는 정확히 필요한 것만)
    cfg.setAllowedOrigins(List.of(
        "http://localhost:8080",
        "http://127.0.0.1:8080"
    ));
    cfg.setAllowedMethods(List.of("GET","POST","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","X-Requested-With"));
    cfg.setExposedHeaders(List.of("Location"));
    cfg.setAllowCredentials(false); // 세션 쿠키를 쓸 거면 true + SameSite=None; Secure(HTTPS) 필요
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
