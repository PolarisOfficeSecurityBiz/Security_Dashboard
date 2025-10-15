package com.polarisoffice.security.service;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.ServiceContactRepository;
import com.polarisoffice.security.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CustomerInfoService {

    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceContactRepository serviceContactRepository;

    /**
     * 로그인한 담당자의 이메일(username)으로 고객사 정보 조회
     */
    public Customer getCustomerInfo(String usernameOrEmail) {
        // 1️⃣ 이메일 기준으로 담당자 조회
        ServiceContact contact = serviceContactRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("담당자 정보를 찾을 수 없습니다. email=" + usernameOrEmail));

        // 2️⃣ 담당자가 속한 고객사(Customer) 직접 참조
        Customer customer = contact.getCustomer();
        if (customer == null) {
            throw new IllegalArgumentException("담당자에 연결된 고객 정보를 찾을 수 없습니다. email=" + usernameOrEmail);
        }

        return customer;
    }

    /**
     * 고객사 ID로 서비스 목록 조회
     */
    public List<Service> getCustomerServices(String customerId) {
        return serviceRepository.findByCustomerId(customerId);
    }
    
    public Customer getCustomerById(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객 정보를 찾을 수 없습니다. customerId=" + customerId));
    }
}
