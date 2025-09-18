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

    @GetMapping("/directad")
    public String directAdPage() {
        return "admin/secuone/directad"; // templates/admin/secuone/directad.html
    }

    @GetMapping("/notice")
    public String noticePage() {
        return "admin/secuone/notice";
    }

    @GetMapping("/polarletter")
    public String polarletterPage() {
        return "admin/secuone/polarletter";
    }

    @GetMapping("/secunews")
    public String secunewsPage() {
        return "admin/secuone/secunews"; // 존재하면
    }
}
