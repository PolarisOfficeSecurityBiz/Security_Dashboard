package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.PolarNotice;
import com.polarisoffice.security.service.PolarNoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/polar-notices")
public class PolarNoticeController {

    private final PolarNoticeService service;

    public PolarNoticeController(PolarNoticeService service) {
        this.service = service;
    }

    @GetMapping
    public List<PolarNotice> getAllNotices() {
        return service.getAll();  // 서비스에서 공지사항을 가져오는 메서드
    }
}
