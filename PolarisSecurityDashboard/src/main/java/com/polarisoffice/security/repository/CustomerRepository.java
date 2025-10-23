// src/main/java/com/polarisoffice/security/repository/CustomerRepository.java
package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Page<Customer> findByCustomerNameContainingIgnoreCase(String keyword, Pageable pageable);
    Optional<Customer> findByCustomerId(String customerId);

    // createAt 필드가 LocalDate 라고 가정. LocalDateTime이면 타입만 LocalDateTime으로 바꿔줘.
    List<Customer> findByCreateAtAfterOrderByCreateAtDesc(LocalDate after);
    List<Customer> findTop10ByCreateAtAfterOrderByCreateAtDesc(LocalDate after);
}
