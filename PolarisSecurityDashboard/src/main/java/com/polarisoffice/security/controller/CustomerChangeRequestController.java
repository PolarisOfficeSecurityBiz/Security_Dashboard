// src/main/java/com/polarisoffice/security/controller/CustomerChangeRequestController.java
package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.ChangeRequestDtos.*;
import com.polarisoffice.security.model.ChangeRequest;
import com.polarisoffice.security.service.ChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer/api/change-requests")
public class CustomerChangeRequestController {

    private final ChangeRequestService service;

    @PostMapping
    public Long create(@RequestBody CreateReq req, Authentication auth) {
        // requesterUserId는 로그인 사용자 id/이메일로
        String requesterUserId = (auth != null ? auth.getName() : null);
        ChangeRequest cr = service.create(req, requesterUserId);
        return cr.getId();
    }
}
