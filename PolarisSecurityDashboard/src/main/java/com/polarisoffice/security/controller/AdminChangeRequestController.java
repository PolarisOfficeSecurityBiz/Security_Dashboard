// src/main/java/com/polarisoffice/security/controller/AdminChangeRequestController.java
package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.ChangeRequestDtos.ItemRes;
import com.polarisoffice.security.dto.ChangeRequestDtos.ReviewReq;
import com.polarisoffice.security.model.ChangeRequest;
import com.polarisoffice.security.service.ChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/change-requests")
public class AdminChangeRequestController {

    private final ChangeRequestService service;

    @GetMapping("/pending")
    public List<ItemRes> listPending() {
        return service.listPending().stream().map(this::map)
                .toList();
    }

    @PostMapping("/{id}/approve")
    public ItemRes approve(@PathVariable Long id, @RequestBody(required = false) ReviewReq req,
                           Authentication auth) {
        String reviewer = (auth != null ? auth.getName() : null);
        ChangeRequest cr = service.approve(id, reviewer, req != null ? req.getAdminComment() : null);
        return map(cr);
    }

    @PostMapping("/{id}/reject")
    public ItemRes reject(@PathVariable Long id, @RequestBody(required = false) ReviewReq req,
                          Authentication auth) {
        String reviewer = (auth != null ? auth.getName() : null);
        ChangeRequest cr = service.reject(id, reviewer, req != null ? req.getAdminComment() : null);
        return map(cr);
    }

    private ItemRes map(ChangeRequest c) {
        return ItemRes.builder()
                .id(c.getId())
                .customerId(c.getCustomer().getCustomerId())
                .serviceId(c.getService() != null ? c.getService().getServiceId() : null)
                .target(c.getTarget())
                .title(c.getTitle())
                .detailsJson(c.getDetailsJson())
                .status(c.getStatus().name())
                .requesterName(c.getRequesterName())
                .requesterEmail(c.getRequesterEmail())
                .createdAt(c.getCreatedAt())
                .reviewedAt(c.getReviewedAt())
                .adminComment(c.getAdminComment())
                .build();
    }
}
