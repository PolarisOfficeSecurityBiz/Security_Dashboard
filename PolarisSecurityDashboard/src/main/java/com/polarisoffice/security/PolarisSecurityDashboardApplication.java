package com.polarisoffice.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.polarisoffice") // 기본 패키지 설정
public class PolarisSecurityDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolarisSecurityDashboardApplication.class, args);
	}

}
