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
     * ✅ 1️⃣ 관리자/고객 공통 로그인 기반 — API 세션 접근 허용
     * Ajax 요청(`/admin/api/**`, `/api/v1/**`)도 세션을 그대로 사용
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new AntPathRequestMatcher("/api/**"))
            .securityMatcher(new AntPathRequestMatcher("/admin/api/**")) // ✅ 관리자 Ajax API 포함
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable()) // JS fetch를 위해 비활성화
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // 공개 API (대시보드용)
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/polar-notices/**",
                        "/api/v1/polar-letters/**",
                        "/api/v1/secu-news/**",
                        "/api/v1/direct-ads/**",
                        "/api/v1/overview").permitAll()

                // 관리자용 API (세션 로그인 후 접근 가능)
                .requestMatchers("/admin/api/**").hasRole("ADMIN")

                // 나머지는 로그인 필요
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
     * ✅ 2️⃣ Web UI (Thymeleaf 페이지용)
     *  - /admin/** → ADMIN
     *  - /customer/** → CUSTOMER
     *  - 나머지는 공개 접근 허용
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/login", "/logout", "/signup", "/admin/signup",
                    "/admin/api/**", "/api/**" // ✅ Ajax 요청은 CSRF 제외
                )
            )
            .authorizeHttpRequests(auth -> auth
                // 공개 리소스
                .requestMatchers(
                    "/", "/login", "/signup", "/admin/signup", "/after-login",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/actuator/health", "/actuator/info"
                ).permitAll()

                // 관리자 페이지
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 고객 페이지
                .requestMatchers("/customer/**").hasRole("CUSTOMER")

                // 기타 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> {
                    // 로그인 성공 후 역할에 따라 분기
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
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    /**
     * ✅ 3️⃣ AuthenticationManager — DelegatingPasswordEncoder 자동 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ 4️⃣ PasswordEncoder (bcrypt 기본)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * ✅ 5️⃣ DaoAuthenticationProvider 등록
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
