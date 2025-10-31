// src/main/java/com/polarisoffice/security/repository/ServiceRepository.java
package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Integer> {

    List<Service> findByCustomerIdOrderByCreateAtDesc(String customerId);

    // ✅ 소속 검증용: 엔티티에 customer(연관) 대신 customerId(칼럼)만 있으므로 이걸 사용
    Optional<Service> findByServiceIdAndCustomerId(Integer serviceId, String customerId);

    long countByProductTypeIgnoreCaseContaining(String keyword);

    long countByProductTypeIgnoreCaseContainingOrProductTypeIgnoreCaseContaining(String k1, String k2);

    List<Service> findByCustomerId(String customerId);
    
    @Query("SELECT s.serviceId, s.serviceName, c.customerName, sc.username AS contactName, s.createAt, c.customerId " +
    	       "FROM Service s " +
    	       "LEFT JOIN Customer c ON s.customerId = c.customerId " +
    	       "LEFT JOIN ServiceContact sc ON s.serviceId = sc.serviceId " +
    	       "WHERE s.licenseId IS NULL")
    	List<Object[]> findUnissuedServices();
   
        Optional<Service> findByDomain(String domain);
 
}
