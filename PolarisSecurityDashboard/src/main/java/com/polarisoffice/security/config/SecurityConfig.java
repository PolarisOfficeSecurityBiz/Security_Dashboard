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
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** ✅ 1) API 보안 체인 (/api/**, /admin/api/**) */
	/** ✅ 1) API 보안 체인 (/api/**, /admin/api/**) */
	@Bean
	@Order(1)
	public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
	    http
	        .securityMatcher(new OrRequestMatcher(
	            new AntPathRequestMatcher("/api/**"),
	            new AntPathRequestMatcher("/admin/api/**")
	        ))
	        .cors(Customizer.withDefaults())
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
	        .authorizeHttpRequests(auth -> auth
	            // ✅ 기존 GET 허용 경로
	            .requestMatchers(HttpMethod.GET,
	                    "/api/v1/polar-notices/**",
	                    "/api/v1/polar-letters/**",
	                    "/api/v1/secu-news/**",
	                    "/api/v1/direct-ads/**",
	                    "/api/v1/overview"
	            ).permitAll()

	            // ✅ 추가: 로그 수집 API 완전 공개
	            .requestMatchers("/api/log/**").permitAll()

	            // ✅ Direct Ads Impression/Click 도 공개 (401 방지)
	            .requestMatchers("/api/directads/**").permitAll()

	            .requestMatchers("/admin/api/**").hasRole("ADMIN")
	            .anyRequest().authenticated()
	        )
	        .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
	        .formLogin(Customizer.withDefaults())
	        .logout(Customizer.withDefaults());

	    return http.build();
	}


    /** ✅ 2) Web UI 체인 (Thymeleaf) */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                // Ajax/외부 처리 경로는 CSRF 제외
                .ignoringRequestMatchers(
                    // 로그인/로그아웃 등
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/logout"),
                    new AntPathRequestMatcher("/signup"),
                    new AntPathRequestMatcher("/admin/signup"),
                    // API는 별도 체인에서 disable 되었지만 혹시 몰라 예외 유지
                    new AntPathRequestMatcher("/admin/api/**"),
                    new AntPathRequestMatcher("/api/**"),
                    // ✅ 릴리즈 노트 수정(모달 저장) CSRF 예외
                    new AntPathRequestMatcher("/admin/vguard/history/*/note", "PATCH")
                )
            )
            .authorizeHttpRequests(auth -> auth
                // 공개 리소스
                .requestMatchers(
                    "/", "/login", "/signup", "/admin/signup", "/after-login",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/actuator/health", "/actuator/info",
                    "/logout" // GET /logout (자동 POST 페이지)
                ).permitAll()

                // 관리자/고객 영역
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
                    var roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
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
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }

    /** ✅ 3) AuthenticationManager */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** ✅ 4) PasswordEncoder (Delegating → bcrypt 기본) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** ✅ 5) DaoAuthenticationProvider */
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
