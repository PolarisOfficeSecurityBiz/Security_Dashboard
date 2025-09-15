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

    /** 회원가입 페이지 (필요 시) */
    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // templates/signup.html (관리자 전용은 /admin/signup 사용)
    }

}
