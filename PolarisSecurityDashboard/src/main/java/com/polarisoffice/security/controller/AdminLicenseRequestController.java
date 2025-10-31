package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.LicenseRequest;
import com.polarisoffice.security.service.LicenseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/license")
@RequiredArgsConstructor
public class AdminLicenseRequestController {

    private final LicenseRequestService licenseRequestService;

    @GetMapping("/requests")
    public String pendingRequests(Model model) {
        model.addAttribute("requests", licenseRequestService.getPendingRequests());
        return "admin/license/request_list";
    }

    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id) {
        licenseRequestService.approveRequest(id);
        return "redirect:/admin/license/requests";
    }
}