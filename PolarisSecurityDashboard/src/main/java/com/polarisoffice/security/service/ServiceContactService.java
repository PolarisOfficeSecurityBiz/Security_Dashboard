package com.polarisoffice.security.service;

import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.ServiceContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceContactService {

    @Autowired
    private ServiceContactRepository serviceContactRepository;

    /**
     * 고객 ID 기준으로 등록된 담당자 1명 조회
     */
    public ServiceContact getByCustomerId(String customerId) {
        return serviceContactRepository.findFirstByCustomer_CustomerId(customerId)
                .orElse(null);
    }

    /**
     * 이메일 기준으로 담당자 조회
     */
    public ServiceContact getByEmail(String email) {
        return serviceContactRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("담당자 정보를 찾을 수 없습니다. email=" + email));
    }
}
