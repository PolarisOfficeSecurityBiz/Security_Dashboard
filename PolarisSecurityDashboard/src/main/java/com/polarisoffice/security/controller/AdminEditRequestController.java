// src/main/java/com/polarisoffice/security/controller/AdminEditRequestController.java
package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.edit.EditRequestResponseDTO;
import com.polarisoffice.security.model.edit.EditRequestStatus;
import com.polarisoffice.security.repository.EditRequestRepository;
import com.polarisoffice.security.service.EditRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
public class AdminEditRequestController {

    // =========================
    // 1) 관리 API (JSON)
    // =========================
    @RestController
    @RequestMapping("/admin/api/edit-requests")
    @RequiredArgsConstructor
    public static class Api {

        private final EditRequestService editRequestService;
        private final EditRequestRepository editRequestRepository;

        /** 목록 조회 (status 필터 선택) */
        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<EditRequestResponseDTO>> list(
                @RequestParam(value = "status", required = false) String statusStr
        ) {
            EditRequestStatus status = parseStatusNullable(statusStr);
            return ResponseEntity.ok(editRequestService.listForAdmin(status));
        }

        /** 상태/메모 갱신 - x-www-form-urlencoded */
        @PatchMapping(
                path = "/{id}",
                consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE
        )
        public ResponseEntity<EditRequestResponseDTO> updateForm(
                @PathVariable Long id,
                @RequestParam("status") String statusStr,
                @RequestParam(value = "memo", required = false) String memo,
                Authentication auth
        ) {
            EditRequestStatus status = parseStatusRequired(statusStr);
            String adminEmail = auth != null ? auth.getName() : "admin";
            return ResponseEntity.ok(editRequestService.updateStatus(id, status, adminEmail, memo));
        }

        /** 상태/메모 갱신 - application/json */
        @PatchMapping(
                path = "/{id}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE
        )
        public ResponseEntity<EditRequestResponseDTO> updateJson(
                @PathVariable Long id,
                @RequestBody UpdateRequest body,
                Authentication auth
        ) {
            if (body == null || body.status == null || body.status.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
            }
            EditRequestStatus status = parseStatusRequired(body.status);
            String adminEmail = auth != null ? auth.getName() : "admin";
            return ResponseEntity.ok(editRequestService.updateStatus(id, status, adminEmail, body.memo));
        }

        // ---- 유틸 & 예외 ----
        private static EditRequestStatus parseStatusNullable(String raw) {
            if (raw == null || raw.isBlank()) return null;
            try {
                return EditRequestStatus.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + raw);
            }
        }

        private static EditRequestStatus parseStatusRequired(String raw) {
            if (raw == null || raw.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
            }
            try {
                return EditRequestStatus.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + raw);
            }
        }

        @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
        public ResponseEntity<String> handleTypeMismatch(
                org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
            return ResponseEntity.badRequest().body("Bad parameter: " + ex.getName());
        }

        /** JSON 요청 바디 DTO */
        public record UpdateRequest(String status, String memo) {}
    }

    // =========================
    // 2) 뷰(Form)용 컨트롤러
    // =========================
    @Controller
    @RequestMapping("/admin/requests")
    @RequiredArgsConstructor
    public static class View {

        private final EditRequestService editRequestService;

        /**
         * 상태 변경 (overview.html의 폼이 POST로 호출)
         * POST /admin/requests/{id}/status
         * form field: status=PENDING|IN_PROGRESS|RESOLVED
         */
        @PostMapping("/{id}/status")
        public String updateStatusByForm(@PathVariable("id") Long id,
                                         @RequestParam("status") EditRequestStatus status,
                                         RedirectAttributes ra) {
            editRequestService.updateStatus(id, status);
            ra.addFlashAttribute("toast", "요청 상태가 변경되었습니다.");
            return "redirect:/admin/overview";
        }

        /**
         * (옵션) 주소창에서 GET으로도 변경하고 싶을 때
         * GET /admin/requests/{id}/status?to=RESOLVED
         */
        @GetMapping("/{id}/status")
        public String updateStatusByGet(@PathVariable("id") Long id,
                                        @RequestParam("to") EditRequestStatus to,
                                        RedirectAttributes ra) {
            editRequestService.updateStatus(id, to);
            ra.addFlashAttribute("toast", "요청 상태가 변경되었습니다.");
            return "redirect:/admin/overview";
        }
    }
}
