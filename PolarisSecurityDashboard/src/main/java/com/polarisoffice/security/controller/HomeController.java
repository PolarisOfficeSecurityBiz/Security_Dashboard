package com.polarisoffice.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
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
        if (authentication == null) return "redirect:/login";
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) return "redirect:/admin/overview";
        if (roles.contains("ROLE_CUSTOMER")) return "redirect:/customer/dashboard";
        return "redirect:/login";
    }

    @GetMapping("/admin/overview")
    public String adminOverview() {
        return "admin/overview";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer/dashboard";
    }
    
    @GetMapping("/admin/logs")
    public String adminLogs() {
    	return "admin/logs";
    }
}
