// src/main/java/com/polarisoffice/security/repository/EditRequestRepository.java
package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.edit.EditRequest;
import com.polarisoffice.security.model.edit.EditRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EditRequestRepository extends JpaRepository<EditRequest, Long> {

    List<EditRequest> findTop20ByOrderByCreateAtDesc();

    List<EditRequest> findTop50ByOrderByCreateAtDesc();

    List<EditRequest> findTop50ByStatusOrderByCreateAtDesc(EditRequestStatus status);

    long countByStatus(EditRequestStatus status);
}
