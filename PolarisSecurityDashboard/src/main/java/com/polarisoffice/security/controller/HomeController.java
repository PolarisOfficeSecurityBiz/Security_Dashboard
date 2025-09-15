package com.polarisoffice.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Set;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/admin/signup")
    public String adminSignupPage() {
        return "admin_signup"; // templates/admin_signup.html
    }

    /** 로그인 성공 후 역할 분기 — 이 매핑은 프로젝트에 단 하나만! */
    @GetMapping("/after-login")
    public String afterLogin(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/overview";
        } else if (roles.contains("ROLE_CUSTOMER")) {
            return "redirect:/customer/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/admin/overview")
    public String adminOverview() {
        return "admin/overview";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer/dashboard";
    }
}
