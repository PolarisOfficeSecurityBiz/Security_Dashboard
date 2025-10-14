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

        // 2️⃣ 담당자가 속한 고객사(Customer) 찾기
        return customerRepository.findByCustomerId(contact.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객 정보를 찾을 수 없습니다. customerId=" + contact.getCustomerId()));
    }

    /**
     * 고객사 ID로 서비스 목록 조회
     */
    public List<Service> getCustomerServices(String customerId) {
        return serviceRepository.findByCustomerId(customerId);
    }
}
