package com.polarisoffice.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping("/customers")
    public String customersPage() {
        // templates/admin/customers.html 을 렌더링
        return "admin/customer/customers";
    }
    
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @GetMapping("/license")
    public String license() {
        return "admin/license/admin_license";
    }

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
