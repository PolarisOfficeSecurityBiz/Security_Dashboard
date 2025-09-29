package com.polarisoffice.security.service;

import org.springframework.stereotype.Service;

import com.polarisoffice.security.dto.MetricsDto;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.ServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceService {

    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    
    public MetricsDto getMetrics() {
        long totalCustomers = customerRepository.count();

        long secuone = serviceRepository
                .countByProductTypeIgnoreCaseContaining("secuone");

        // 필요 시 V-Guard도 키워드 여러 개 허용
        long vguard = serviceRepository
                .countByProductTypeIgnoreCaseContainingOrProductTypeIgnoreCaseContaining("v-guard", "vguard");

        return new MetricsDto(totalCustomers, vguard, secuone);
    }
}
