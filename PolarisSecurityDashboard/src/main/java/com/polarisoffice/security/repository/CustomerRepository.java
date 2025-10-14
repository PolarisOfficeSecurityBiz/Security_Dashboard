package com.polarisoffice.security.repository;


import com.polarisoffice.security.model.Customer;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    // 🔍 고객명 검색용 (관리자 페이지용)
    Page<Customer> findByCustomerNameContainingIgnoreCase(String keyword, Pageable pageable);

    // 🔍 ID로 조회 (Spring Data 기본 findById(String)과 동일하지만 명시적으로 유지)
    Optional<Customer> findByCustomerId(String customerId);


}