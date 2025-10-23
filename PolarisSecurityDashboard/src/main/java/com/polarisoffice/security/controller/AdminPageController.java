package com.polarisoffice.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

//    // 기존 경로인 /license를 그대로 사용
//    @GetMapping("/license")
//    public String license() {
//        return "admin/license/admin_license"; // "admin/license/admin_license" 템플릿 반환
//    }

//    @GetMapping("/vguard")
//    public String vguard() {
//        return "admin/vguard/vguard";
//    }

    // ✅ 이거 삭제해야 합니다.
    // @GetMapping("/secuone")
    // public String secuone() {
    //     return "admin/secuone";
    // }
}
