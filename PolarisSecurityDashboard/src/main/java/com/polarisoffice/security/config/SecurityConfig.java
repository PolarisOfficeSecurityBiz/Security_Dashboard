// src/main/java/com/polarisoffice/security/config/SecurityConfig.java
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * ✅ 1) API 보안 체인 (/api/**, /admin/api/**)
     *  - Ajax는 세션 사용, 인증 필요시 401 반환
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new AntPathRequestMatcher("/api/**"))
            .securityMatcher(new AntPathRequestMatcher("/admin/api/**"))
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable()) // fetch/axios 편의
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // 공개 API (필요시만)
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/polar-notices/**",
                        "/api/v1/polar-letters/**",
                        "/api/v1/secu-news/**",
                        "/api/v1/direct-ads/**",
                        "/api/v1/overview"
                ).permitAll()

                // 관리자 API
                .requestMatchers("/admin/api/**").hasRole("ADMIN")

                // 나머지
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .formLogin(Customizer.withDefaults())
            .logout(Customizer.withDefaults());

        return http.build();
    }

    /**
     * ✅ 2) Web UI 체인 (Thymeleaf)
     *  - GET /logout 는 우리의 자동 POST 뷰가 처리 → permitAll
     *  - POST /logout 는 Security가 처리 → CSRF 토큰 또는 무시 예외 중 택1
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                // Ajax/외부 처리 경로는 CSRF 제외 (이미 토큰 사용 중이라면 빼도 됨)
                .ignoringRequestMatchers(
                    "/login", "/logout", "/signup", "/admin/signup",
                    "/admin/api/**", "/api/**"
                )
            )
            .authorizeHttpRequests(auth -> auth
                // 공개 리소스
                .requestMatchers(
                    "/", "/login", "/signup", "/admin/signup", "/after-login",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/actuator/health", "/actuator/info",
                    "/logout" // ✅ GET /logout (자동 POST 제출 페이지) 허용
                ).permitAll()

                // 관리자 페이지
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 고객 페이지
                .requestMatchers("/customer/**").hasRole("CUSTOMER")

                // 기타
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> {
                    var roles = auth.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .toList();
                    if (roles.contains("ROLE_ADMIN")) {
                        res.sendRedirect("/admin/overview");
                    } else if (roles.contains("ROLE_CUSTOMER")) {
                        res.sendRedirect("/customer/dashboard");
                    } else {
                        res.sendRedirect("/after-login");
                    }
                })
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")                 // 기본: POST /logout (필터가 처리)
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }

    /**
     * ✅ 3) AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ 4) PasswordEncoder (Delegating → bcrypt 기본)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * ✅ 5) DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
