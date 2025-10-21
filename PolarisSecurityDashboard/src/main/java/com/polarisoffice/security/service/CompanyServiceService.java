package com.polarisoffice.security.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.polarisoffice.security.dto.MetricsDto;
import com.polarisoffice.security.dto.RecentCustomerRow;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.ServiceRepository;

import jakarta.transaction.Transactional;
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
    
    /**
     * 최근 3개월 내 등록 고객사 (최대 50개 등 원하면 제한)
     */

    public List<RecentCustomerRow> getRecentCustomers3Months() {
    	LocalDate after = LocalDate.now().minusMonths(3);   // ✅ LocalDate 로 생성

    	return customerRepository.findByCreateAtAfterOrderByCreateAtDesc(after)
            .stream()
            .map(c -> new RecentCustomerRow(
                    c.getCustomerId(),
                    c.getCustomerName(),
                    c.getConnectedCompany() != null ? c.getConnectedCompany().getCustomerName() : null,
                    c.getCreateAt()   // LocalDate → RecentCustomerRow 보조 생성자에서 문자열로 변환
            ))
            .toList();
    }

}
