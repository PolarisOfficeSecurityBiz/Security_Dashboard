package com.polarisoffice.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/secuone")
public class SecuOnePageController {

    @GetMapping
    public String secuoneOverview() {
        return "admin/secuone/secuone"; // templates/admin/secuone/secuone.html
    }

    @GetMapping("/secunews")        // ✅ 클래스 prefix가 /admin/secuone 이라 여기엔 /secunews 만!
    public String secuoneNewsPage() {
        return "admin/secuone/secunews"; // templates/admin/secuone/secunews.html
    }

    @GetMapping("/directad")
    public String directAdPage() { return "admin/secuone/directad"; }

    @GetMapping("/notice")
    public String noticePage() { return "admin/secuone/notice"; }

    @GetMapping("/polarletter")
    public String polarletterPage() { return "admin/secuone/polarletter"; }
}
