package com.polarisoffice.security.service;

import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * 고객사 ID 기준 서비스 목록 조회
     */
    public List<Service> getServicesByCustomerId(String customerId) {
        return serviceRepository.findByCustomerId(customerId);
    }
}
