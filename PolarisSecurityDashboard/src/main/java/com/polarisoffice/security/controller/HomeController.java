package com.polarisoffice.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/after-login")
    public String afterLogin(Authentication authentication) {
        if (authentication == null) {
            System.out.println("❌ 인증 정보 없음 (authentication is null)");
            return "redirect:/login";
        }

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        System.out.println("✅ 로그인 성공, roles = " + roles);

        if (roles.contains("ROLE_ADMIN")) {
            System.out.println("➡ 관리자 페이지로 이동");
            return "redirect:/admin/overview";
        }
        if (roles.contains("ROLE_CUSTOMER")) {
            System.out.println("➡ 고객사 대시보드로 이동");
            return "redirect:/customer/dashboard";
        }

        System.out.println("❌ 권한 불명, 로그인으로 리다이렉트");
        return "redirect:/login";
    }

    
    @GetMapping("/admin/logs")
    public String adminLogs() {
    	return "admin/logs";
    }
//    @GetMapping("/customer/dashboard")
//    public String customerDashboard(Model model, Authentication auth) {
//        model.addAttribute("path", "/customer/dashboard");
//        model.addAttribute("username", auth != null ? auth.getName() : null);
//        return "customer/dashboard";
//    }
//    
    

}
