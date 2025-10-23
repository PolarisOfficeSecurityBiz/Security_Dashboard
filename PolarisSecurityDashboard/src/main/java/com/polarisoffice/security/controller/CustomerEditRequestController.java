// src/main/java/com/polarisoffice/security/controller/customer/CustomerEditRequestController.java
package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.edit.CreateCompanyEditRequestDTO;
import com.polarisoffice.security.dto.edit.CreateServiceEditRequestDTO;
import com.polarisoffice.security.dto.edit.EditRequestResponseDTO;
import com.polarisoffice.security.service.EditRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/edit-requests")
@RequiredArgsConstructor
public class CustomerEditRequestController {

    private final EditRequestService editRequestService;

    /** 회사 정보 수정요청 */
    @PostMapping("/company")
    public ResponseEntity<EditRequestResponseDTO> company(@RequestBody CreateCompanyEditRequestDTO dto,
                                                          Authentication auth) {
        String fallbackEmail = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(editRequestService.createCompanyRequest(dto, fallbackEmail));
    }

    /** 서비스 정보 수정요청 */
    @PostMapping("/service")
    public ResponseEntity<EditRequestResponseDTO> service(@RequestBody CreateServiceEditRequestDTO dto,
                                                          Authentication auth) {
        String fallbackEmail = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(editRequestService.createServiceRequest(dto, fallbackEmail));
    }
}
