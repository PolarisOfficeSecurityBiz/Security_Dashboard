package com.polarisoffice.security.service;

import com.polarisoffice.security.dto.ChangeRequestDtos.*;
import com.polarisoffice.security.model.ChangeRequest;
import com.polarisoffice.security.model.ChangeRequest.RequestStatus;
import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.ChangeRequestRepository;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ChangeRequestService {
    private final ChangeRequestRepository repo;
    private final CustomerRepository customerRepo;
    private final ServiceRepository serviceRepo;

    public ChangeRequest create(CreateReq req, String requesterUserId) {
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객사가 없습니다."));

        Service service = null;
        if (req.getTarget() == ChangeRequest.RequestTarget.SERVICE) {
            if (req.getServiceId() == null)
                throw new IllegalArgumentException("서비스 수정요청은 serviceId가 필요합니다.");
            service = serviceRepo.findById(req.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("서비스가 없습니다."));
            // (선택) 소유 검증: service.getCustomerId() == req.customerId
        }

        ChangeRequest entity = ChangeRequest.builder()
                .customer(customer)
                .service(service)
                .target(req.getTarget())
                .status(RequestStatus.PENDING)
                .title(req.getTitle())
                .detailsJson(req.getDetailsJson())
                .requesterUserId(requesterUserId)
                .requesterName(req.getRequesterName())
                .requesterEmail(req.getRequesterEmail())
                .createdAt(LocalDateTime.now())
                .build();

        return repo.save(entity);
    }

    public List<ChangeRequest> listPending() {
        return repo.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING);
    }

    public ChangeRequest approve(Long id, String reviewerUserId, String comment) {
        ChangeRequest cr = repo.findById(id).orElseThrow();
        cr.setStatus(RequestStatus.APPROVED);
        cr.setReviewerUserId(reviewerUserId);
        cr.setAdminComment(comment);
        cr.setReviewedAt(LocalDateTime.now());
        return repo.save(cr);
    }

    public ChangeRequest reject(Long id, String reviewerUserId, String comment) {
        ChangeRequest cr = repo.findById(id).orElseThrow();
        cr.setStatus(RequestStatus.REJECTED);
        cr.setReviewerUserId(reviewerUserId);
        cr.setAdminComment(comment);
        cr.setReviewedAt(LocalDateTime.now());
        return repo.save(cr);
    }
}
