package com.polarisoffice.security.service;

import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.ServiceRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;
    
    // 수정: assignedTo 필드를 사용하지 않고 customerId로 서비스 목록을 조회
    public List<Service> getServicesByCustomerId(String customerId) {
        // 이제 customerId만 사용하여 서비스를 조회
        return serviceRepository.findByCustomerId(customerId);
    }

    public Service getPrimaryService(String customerId) {
        return serviceRepository.findByCustomerIdOrderByCreateAtDesc(customerId)
                .stream().findFirst().orElse(null);
    }

    public Service getById(Integer serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("서비스 정보를 찾을 수 없습니다."));
    }

    /** ✅ 소속 검증 포함 조회 (customerId 컬럼 기반) */
    public Service getByIdAndCustomer(Integer serviceId, String customerId) {
        return serviceRepository.findByServiceIdAndCustomerId(serviceId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 고객의 서비스 정보를 찾을 수 없습니다."));
    }
    
    @Transactional
    public Service createService(String customerId, String serviceName, String domain, String productType, 
                                 Integer cpiValue, Double rsRate) {
        Service newService = Service.builder()
                .serviceName(serviceName)
                .domain(domain)
                .productType(productType)
                .cpiValue(cpiValue)
                .rsRate(rsRate)
                .createAt(LocalDate.now()) // 서비스 추가일을 현재 날짜로 설정
                .updateAt(LocalDate.now()) // 서비스 업데이트일을 현재 날짜로 설정
                .customerId(customerId)
                .licenseId(null)  // 라이선스 ID는 현재 null로 설정, 필요 시 수정
                .build();

        return serviceRepository.save(newService);
    }
    
 // ServiceService.java 에 추가
    public List<Service> getByIds(List<Integer> ids) {
        return ids == null || ids.isEmpty()
                ? List.of()
                : serviceRepository.findAllById(ids);
    }

}
