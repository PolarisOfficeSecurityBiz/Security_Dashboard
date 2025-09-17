package com.polarisoffice.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
public class ThymeleafConfig {

    /** Thymeleaf Layout Dialect 등록 (layout:decorate / layout:fragment 사용 가능) */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    /** (선택) Spring Security Dialect - sec:authorize 등 사용 시 */
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}