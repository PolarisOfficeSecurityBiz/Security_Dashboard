// src/main/java/com/polarisoffice/security/repository/ChangeRequestRepository.java
package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.ChangeRequest;
import com.polarisoffice.security.model.ChangeRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    List<ChangeRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    List<ChangeRequest> findByCustomer_CustomerIdOrderByCreatedAtDesc(String customerId);

    List<ChangeRequest> findByService_ServiceIdOrderByCreatedAtDesc(Integer serviceId);
}
