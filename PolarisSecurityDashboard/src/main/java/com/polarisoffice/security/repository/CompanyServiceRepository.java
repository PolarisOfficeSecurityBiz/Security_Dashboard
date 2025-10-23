package com.polarisoffice.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.polarisoffice.security.model.CustomerService;

public interface CompanyServiceRepository extends JpaRepository<CustomerService, Long> {

    // 서비스 레코드 수
    long countByServiceName(String serviceName);

    // 서비스명을 가진 "고객사 수" (중복 제거)
    @Query("""
           select count(distinct cs.customer.customerId)
           from CustomerService cs
           where cs.serviceName = :serviceName
           """)
    long countDistinctCustomerByServiceName(String serviceName);
    
}
