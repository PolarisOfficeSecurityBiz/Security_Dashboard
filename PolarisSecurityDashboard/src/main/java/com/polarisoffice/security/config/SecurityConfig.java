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

    /* =======================================================================
     * API 체인 (/api/**)
     *  - 로그 수집: POST /api/logs/** → permitAll
     *  - 로그 리포트: GET /api/logs/report → (여기서는 permitAll, 필요하면 authenticated로 교체)
     *  - 그 외는 기존 정책 유지
     * ======================================================================= */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new AntPathRequestMatcher("/api/**"))
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable()) // 수집용 REST는 CSRF 비활성화
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // CORS preflight 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ====== 로그 수집/조회 ======
                .requestMatchers(HttpMethod.POST, "/api/logs/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/logs/report").permitAll()
                // 필요 시: .requestMatchers(HttpMethod.GET, "/api/logs/report").authenticated()

                // ====== 공개 GET API ======
                .requestMatchers(HttpMethod.GET, "/api/v1/polar-notices", "/api/v1/polar-notices/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/polar-letters", "/api/v1/polar-letters/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/secu-news",   "/api/v1/secu-news/**").permitAll() // 오타 수정
                .requestMatchers(HttpMethod.GET, "/api/v1/direct-ads",  "/api/v1/direct-ads/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/overview").permitAll()

                // ====== 쓰기(ADMIN) ======
                .requestMatchers(HttpMethod.POST,   "/api/v1/polar-notices/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/polar-notices/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/polar-notices/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,   "/api/v1/polar-letters/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/polar-letters/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/polar-letters/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,   "/api/v1/secu-news/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/secu-news/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/secu-news/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST,   "/api/v1/direct-ads/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/direct-ads/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/direct-ads/**").hasRole("ADMIN")

                // 나머지 API는 인증 필요
                .anyRequest().authenticated()
            )
            // API는 리다이렉트 대신 401/403
            .exceptionHandling(e -> e.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            // stateless + Basic (필요 시 사용)
            .httpBasic(Customizer.withDefaults())
            .formLogin(f -> f.disable())
            .logout(l -> l.disable());

        return http.build();
    }

    /* =======================================================================
     * 웹 체인 (그 외)
     * ======================================================================= */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http,
                                           DaoAuthenticationProvider provider) throws Exception {
        http
            .authenticationProvider(provider)
            .cors(Customizer.withDefaults())
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
                // 나머지는 인증
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

    /* =======================================================================
     * 인증/암호화 빈
     * ======================================================================= */
    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance()); // 운영에선 제거 권장

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
