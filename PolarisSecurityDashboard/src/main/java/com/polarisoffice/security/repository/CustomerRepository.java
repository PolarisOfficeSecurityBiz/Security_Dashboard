package com.polarisoffice.security.repository;


import com.polarisoffice.security.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Page<Customer> findByCustomerNameContainingIgnoreCase(String keyword, Pageable pageable);
}