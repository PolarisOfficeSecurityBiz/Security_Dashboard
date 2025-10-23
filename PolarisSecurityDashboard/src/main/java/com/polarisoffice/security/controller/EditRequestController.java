package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.edit.CreateCompanyEditRequestDTO;
import com.polarisoffice.security.dto.edit.CreateServiceEditRequestDTO;
import com.polarisoffice.security.dto.edit.EditRequestCreateRequest;
import com.polarisoffice.security.dto.edit.EditRequestResponseDTO;
import com.polarisoffice.security.model.edit.EditRequest;
import com.polarisoffice.security.model.edit.EditRequestStatus;
import com.polarisoffice.security.model.edit.EditTargetType;
import com.polarisoffice.security.service.EditRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EditRequestController {

    private final EditRequestService service;

    /**
     * 고객사가 “수정요청” 제출
     * POST /customer/requests
     */
 // EditRequestController.java

    @PostMapping("/customer/requests")
    public ResponseEntity<?> create(@RequestBody EditRequestCreateRequest req,
                                    Authentication auth) {
        if (req.getCustomerId() == null || req.getCustomerId().isBlank()) {
            return ResponseEntity.badRequest().body("customerId is required");
        }
        if (req.getTargetType() == null) {
            return ResponseEntity.badRequest().body("targetType is required");
        }
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body("message is required");
        }

        String requesterEmail = auth != null ? auth.getName() : null;
        String requesterName  = null; // 필요시 principal에서 꺼내거나 별도 조회

        EditRequestResponseDTO saved; // 타입을 EditRequestResponseDTO로 변경
        try {
            // targetType을 Enum으로 처리
            EditTargetType targetType = EditTargetType.valueOf(req.getTargetType().toUpperCase());

            switch (targetType) {
                case COMPANY:
                    // 회사 정보 수정 요청 처리
                    CreateCompanyEditRequestDTO companyDto = new CreateCompanyEditRequestDTO();
                    companyDto.setCustomerId(req.getCustomerId());
                    companyDto.setContent(req.getMessage());
                    saved = service.createCompanyRequest(companyDto, requesterEmail);  // DTO 전달
                    break;

                 // targetType이 SERVICE일 때 서비스 수정 요청 처리
                case SERVICE:
                    if (req.getTargetId() == null) {
                        return ResponseEntity.badRequest().body("service targetId is required");
                    }

                    // CreateServiceEditRequestDTO 객체를 생성
                    CreateServiceEditRequestDTO serviceDto = new CreateServiceEditRequestDTO();
                    serviceDto.setCustomerId(req.getCustomerId());
                    serviceDto.setServiceId(req.getTargetId());  // serviceId 설정
                    serviceDto.setContent(req.getMessage());

                    // service.createServiceRequest() 호출
                    saved = service.createServiceRequest(serviceDto, requesterEmail);
                    break;


                default:
                    return ResponseEntity.badRequest().body("invalid targetType");
            }
        } catch (IllegalArgumentException e) {
            // 잘못된 targetType에 대한 처리
            return ResponseEntity.badRequest().body("invalid targetType");
        }

        return ResponseEntity.ok(saved.getId()); // 이제 saved는 EditRequestResponseDTO 객체임
    }

}
