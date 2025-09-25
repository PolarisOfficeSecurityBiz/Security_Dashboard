package com.polarisoffice.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /* ----------------------- API 체인 (/api/**) ----------------------- */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new AntPathRequestMatcher("/api/**"))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // CORS preflight 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 공개 API
                .requestMatchers("/api/logs/**").permitAll()
                // 나머지 API는 인증 필요 (원하면 더 개방 가능)
                .anyRequest().authenticated()
            )
            // API는 리다이렉트 대신 401/403만
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            // 필요 시 토큰 없을 때 간단 인증용
            .httpBasic(Customizer.withDefaults())
            // 폼 로그인/로그아웃 비활성화
            .formLogin(f -> f.disable())
            .logout(l -> l.disable());

        return http.build();
    }

    /* ----------------------- 웹 체인 (그 외) ----------------------- */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http,
                                           DaoAuthenticationProvider provider) throws Exception {
        http
            .authenticationProvider(provider)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Swagger / OpenAPI
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // 헬스체크
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // 정적/공용
                .requestMatchers(
                    "/", "/login", "/signup", "/admin/signup", "/after-login",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error"
                ).permitAll()
                // 권한별 페이지
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                // 그 외는 인증
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> res.sendRedirect("/after-login"))
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    /* ----------------------- 인증/암호화 빈 ----------------------- */
    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        // 운영에선 noop 제거 권장
        encoders.put("noop", NoOpPasswordEncoder.getInstance());

        DelegatingPasswordEncoder delegating =
                new DelegatingPasswordEncoder(idForEncode, encoders);
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegating;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider(
            UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }
}
