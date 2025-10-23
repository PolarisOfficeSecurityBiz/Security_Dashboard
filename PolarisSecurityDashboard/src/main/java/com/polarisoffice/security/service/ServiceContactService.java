package com.polarisoffice.security.service;

import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.ServiceContactRepository;

import java.util.List;

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

    /**
     * 고객 ID와 담당자 username을 기반으로 서비스 담당자 정보 조회
     * @param customerId 고객 ID
     * @param username 담당자 username
     * @return 고객 ID와 username에 해당하는 담당자 목록
     */
    public List<ServiceContact> getServiceContactByCustomerAndUsername(String customerId, String username) {
        // 고객 ID와 username으로 해당 서비스를 담당하는 담당자 정보를 조회
        return serviceContactRepository.findByCustomer_CustomerIdAndUsername(customerId, username);
    }

    /**
     * 고객 ID와 서비스 ID를 기준으로 서비스 담당자 목록 조회
     * @param customerId 고객 ID
     * @param serviceId 서비스 ID
     * @return 특정 고객과 서비스 ID에 해당하는 담당자 목록
     */
    public List<ServiceContact> getServiceContactByCustomerAndService(String customerId, Integer serviceId) {
        return serviceContactRepository.findByCustomer_CustomerIdAndServiceId(customerId, serviceId);
    }

    /**
     * 특정 서비스 ID와 담당자 email을 기준으로 담당자 정보 조회
     * @param serviceId 서비스 ID
     * @param email 담당자 email
     * @return 특정 서비스의 담당자 정보
     */
    public List<ServiceContact> getServiceContactByServiceIdAndEmail(Integer serviceId, String email) {
        return serviceContactRepository.findServiceContactByServiceIdAndEmail(serviceId, email);
    }
}
