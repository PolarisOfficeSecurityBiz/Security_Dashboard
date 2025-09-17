package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.ServiceContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceContactRepository extends JpaRepository<ServiceContact, Integer> {
    List<ServiceContact> findByCustomerIdAndServiceIdOrderByCreateAtDesc(String customerId, Integer serviceId);
    long deleteByCustomerId(String customerId);              // 고객사 삭제 시 일괄 삭제
    long deleteByCustomerIdAndServiceId(String cid, Integer sid);

    // 선택 사용 (필요 시)
    List<ServiceContact> findByCustomerIdAndServiceId(String customerId, Integer serviceId);
    List<ServiceContact> findByServiceId(Integer serviceId);
}
