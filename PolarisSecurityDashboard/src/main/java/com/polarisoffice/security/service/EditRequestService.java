// src/main/java/com/polarisoffice/security/service/EditRequestService.java
package com.polarisoffice.security.service;

import com.polarisoffice.security.dto.edit.CreateCompanyEditRequestDTO;
import com.polarisoffice.security.dto.edit.CreateServiceEditRequestDTO;
import com.polarisoffice.security.dto.edit.EditRequestResponseDTO;
import com.polarisoffice.security.model.edit.EditRequest;
import com.polarisoffice.security.model.edit.EditRequestStatus;
import com.polarisoffice.security.model.edit.EditTargetType;
import com.polarisoffice.security.repository.EditRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EditRequestService {

    private final EditRequestRepository repository;

    // 선택 의존성 아님: 지금은 MailService 구현이 있으니 그냥 주입받아 사용
    private final MailService mailService;

    /** 고객사 정보 수정요청 생성 */
    @Transactional
    public EditRequestResponseDTO createCompanyRequest(CreateCompanyEditRequestDTO dto,
                                                       String fallbackEmail) {
        String requesterName  = fallbackEmail;
        String requesterEmail = fallbackEmail;

        EditRequest req = EditRequest.builder()
                .customerId(dto.getCustomerId())
                .targetType(EditTargetType.COMPANY)
                .content(dto.getContent())
                .requesterName(requesterName)
                .requesterEmail(requesterEmail)
                .status(EditRequestStatus.PENDING)
                .build();

        return toDTO(repository.save(req));
    }

    /** 서비스 정보 수정요청 생성 */
    @Transactional
    public EditRequestResponseDTO createServiceRequest(CreateServiceEditRequestDTO dto,
                                                       String fallbackEmail) {
        String requesterName  = fallbackEmail;
        String requesterEmail = fallbackEmail;

        EditRequest req = EditRequest.builder()
                .customerId(dto.getCustomerId())
                .serviceId(dto.getServiceId())
                .targetType(EditTargetType.SERVICE)
                .content(dto.getContent())
                .requesterName(requesterName)
                .requesterEmail(requesterEmail)
                .status(EditRequestStatus.PENDING)
                .build();

        return toDTO(repository.save(req));
    }

    /** 최근 20건 */
    @Transactional(readOnly = true)
    public List<EditRequest> getLatestTop20() {
        return repository.findTop20ByOrderByCreateAtDesc();
    }

    /** 상태별 카운트 */
    @Transactional(readOnly = true)
    public long countByStatus(EditRequestStatus st) {
        return repository.countByStatus(st);
    }

    /** 목록 조회(관리자) */
    @Transactional(readOnly = true)
    public List<EditRequestResponseDTO> listForAdmin(EditRequestStatus status) {
        List<EditRequest> rows = (status == null)
                ? repository.findTop50ByOrderByCreateAtDesc()
                : repository.findTop50ByStatusOrderByCreateAtDesc(status);
        return rows.stream().map(this::toDTO).toList();
    }

    /** 관리자: 상태 업데이트 + 처리자/메모 (메일 포함) */
    @Transactional
    public EditRequestResponseDTO updateStatus(Long id,
                                               EditRequestStatus status,
                                               String adminEmail,
                                               String memo) {
        EditRequest req = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("EditRequest not found: " + id));

        req.setStatus(status);
        req.setHandledBy(adminEmail);
        req.setHandledAt(LocalDateTime.now());
        if (memo != null) req.setAdminMemo(memo);

        EditRequest saved = repository.save(req);

        if (status == EditRequestStatus.RESOLVED) {
            sendResolvedMail(saved);
        }
        return toDTO(saved);
    }

    /** 뷰(Form)에서 호출하는 단순 버전 (메일 포함) */
    @Transactional
    public void updateStatus(Long id, EditRequestStatus status) {
        EditRequest req = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정요청을 찾을 수 없습니다. id=" + id));

        req.setStatus(status);
        if (status == EditRequestStatus.RESOLVED) {
            req.setHandledAt(LocalDateTime.now());
        }
        EditRequest saved = repository.save(req);

        if (status == EditRequestStatus.RESOLVED) {
            sendResolvedMail(saved);
        }
    }

    /* 메일 발송 내부 유틸 */
    private void sendResolvedMail(EditRequest req) {
        try {
            if (req.getRequesterEmail() != null && !req.getRequesterEmail().isBlank()) {
                mailService.sendEditRequestResolved(req.getRequesterEmail(), req.getRequesterName(), req);
            }
        } catch (Exception e) {
            System.err.println("완료 메일 발송 실패: " + e.getMessage());
        }
    }

    /* DTO 변환 */
    private EditRequestResponseDTO toDTO(EditRequest e) {
        return EditRequestResponseDTO.builder()
                .id(e.getId())
                .customerId(e.getCustomerId())
                .serviceId(e.getServiceId())
                .targetType(e.getTargetType())
                .content(e.getContent())
                .requesterName(e.getRequesterName())
                .requesterEmail(e.getRequesterEmail())
                .status(e.getStatus())
                .createAt(e.getCreateAt())
                .handledAt(e.getHandledAt())
                .handledBy(e.getHandledBy())
                .adminMemo(e.getAdminMemo())
                .build();
    }
}
