package com.polarisoffice.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
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
     * 1️⃣ REST API용 보안 설정
     *  - JWT, 외부 연동용 등 상태 없는 요청
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher(new AntPathRequestMatcher("/api/**"))
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/logs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/logs/report").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/polar-notices/**",
                    "/api/v1/polar-letters/**",
                    "/api/v1/secu-news/**",
                    "/api/v1/direct-ads/**",
                    "/api/v1/overview").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .httpBasic(Customizer.withDefaults())
            .formLogin(f -> f.disable())
            .logout(l -> l.disable());

        return http.build();
    }

    /**
     * 2️⃣ 웹 페이지용 보안 설정
     *  - /admin/** → 관리자
     *  - /customer/** → 고객
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/login", "/logout", "/signup", "/admin/signup"))
            .authorizeHttpRequests(auth -> auth
                // 공개 자원
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(
                    "/", "/login", "/signup", "/admin/signup", "/after-login",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                // 권한 구분
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> {
                    // 로그인 성공 후 역할에 따라 페이지 분기
                    var authorities = auth.getAuthorities().stream()
                            .map(a -> a.getAuthority()).toList();
                    if (authorities.contains("ROLE_ADMIN")) {
                        res.sendRedirect("/admin/overview");
                    } else if (authorities.contains("ROLE_CUSTOMER")) {
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
     * 3️⃣ 사용자 인증 관리자
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 4️⃣ PasswordEncoder — {bcrypt}, {noop}, 등 prefix 자동 인식
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 5️⃣ DaoAuthenticationProvider 등록
     *     → CustomUserDetailsService를 Spring Security와 연결
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
