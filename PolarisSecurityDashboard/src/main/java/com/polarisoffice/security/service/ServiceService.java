package com.polarisoffice.security.service;

import com.polarisoffice.security.dto.ServiceUnlessDto;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    /** ✅ 고객 ID 기준 서비스 목록 조회 */
    public List<Service> getServicesByCustomerId(String customerId) {
        return serviceRepository.findByCustomerId(customerId);
    }

    /** ✅ 가장 최근 등록된 서비스(대표 서비스) 조회 */
    public Service getPrimaryService(String customerId) {
        return serviceRepository.findByCustomerIdOrderByCreateAtDesc(customerId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    /** ✅ 서비스 단일 조회 */
    public Service getById(Integer serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("서비스 정보를 찾을 수 없습니다."));
    }

    /** ✅ 고객 검증 포함 서비스 조회 */
    public Service getByIdAndCustomer(Integer serviceId, String customerId) {
        return serviceRepository.findByServiceIdAndCustomerId(serviceId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 고객의 서비스 정보를 찾을 수 없습니다."));
    }

    /** ✅ 신규 서비스 생성 */
    @Transactional
    public Service createService(String customerId,
                                 String serviceName,
                                 String domain,
                                 String productType,
                                 Integer cpiValue,
                                 Double rsRate) {

        Service newService = Service.builder()
                .serviceName(serviceName)
                .domain(domain)
                .productType(productType)
                .cpiValue(cpiValue)
                .rsRate(rsRate)
                .createAt(LocalDate.now())
                .updateAt(LocalDate.now())
                .customerId(customerId)
                .licenseId(null) // 최초 생성 시 미발급 상태
                .build();

        return serviceRepository.save(newService);
    }

    /** ✅ ID 리스트로 여러 서비스 조회 */
    public List<Service> getByIds(List<Integer> ids) {
        return (ids == null || ids.isEmpty())
                ? List.of()
                : serviceRepository.findAllById(ids);
    }

    // ==========================================================
    // 🧩 추가: 관리자 대시보드용 - "미발급 서비스 목록"
    // ==========================================================

    /**
     * 라이선스가 아직 발급되지 않은 서비스 목록 조회
     * (licenseId == null)
     */
    public List<ServiceUnlessDto> getUnissuedServices() {
        List<Object[]> results = serviceRepository.findUnissuedServices();

        List<ServiceUnlessDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            Integer serviceId = (Integer) row[0];
            String serviceName = (String) row[1];
            String customerName = (String) row[2];
            String contactName = (String) row[3];
            LocalDate createAt = (LocalDate) row[4];
            String customerId = (String) row[5];

            dtos.add(new ServiceUnlessDto(serviceId, serviceName, customerName, contactName, createAt, customerId));
        }

        return dtos;
    }

    /**
     * 단순 필터 기반 미발급 서비스 조회 (엔티티 기반)
     * → DTO 변환 없이 Service 리스트로 받고 싶을 때 사용
     */
    public List<Service> getUnissuedServiceEntities() {
        return serviceRepository.findAll().stream()
                .filter(s -> s.getLicenseId() == null)
                .collect(Collectors.toList());
    }
}
