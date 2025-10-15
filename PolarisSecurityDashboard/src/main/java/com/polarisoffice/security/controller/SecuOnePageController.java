package com.polarisoffice.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/secuone")
public class SecuOnePageController {

    @GetMapping("")
    public String secuoneOverview() {
        return "admin/secuone/secuone";
    }

    @GetMapping("/secunews")
    public String secuoneNewsPage() {
        return "admin/secuone/secunews";
    }

    @GetMapping("/directad")
    public String directAdPage() {
        return "admin/secuone/directad";
    }

    @GetMapping("/notice")
    public String noticePage() {
        return "admin/secuone/notice";
    }

    @GetMapping("/polarletter")
    public String polarletterPage() {
        return "admin/secuone/polarletter";
    }
}
