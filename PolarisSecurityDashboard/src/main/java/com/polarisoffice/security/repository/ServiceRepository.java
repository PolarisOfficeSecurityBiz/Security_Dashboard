package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findByCustomerIdOrderByCreateAtDesc(String customerId);
    Optional<Service> findByServiceIdAndCustomerId(Integer serviceId, String customerId);
    // "SecuOne"가 들어간 모든 상품유형 카운트
    long countByProductTypeIgnoreCaseContaining(String keyword);
    // "V-Guard" 카운트도 필요하면
    long countByProductTypeIgnoreCaseContainingOrProductTypeIgnoreCaseContaining(String k1, String k2);
}