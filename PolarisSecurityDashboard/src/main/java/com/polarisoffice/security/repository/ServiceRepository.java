package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findByCustomerIdOrderByCreateAtDesc(String customerId);
    Optional<Service> findByServiceIdAndCustomerId(Integer serviceId, String customerId);
}