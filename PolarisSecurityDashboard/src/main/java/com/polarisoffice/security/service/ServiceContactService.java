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
        return serviceContactRepository.findFirstByCustomerId(customerId)
                .orElse(null);
    }
}
