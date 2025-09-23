package com.polarisoffice.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    /** 로그인 페이지 */
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }

    @GetMapping("/admin/signup")
    public String adminSignupPage() {
        // templates/admin/signup.html 을 렌더
        return "admin_signup";
    }

}
