package com.polarisoffice.security.service;

import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.ServiceContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Impl 분리 없이 단일 Service 클래스.
 * - 기존 코드 호환을 위해 과거 메서드명을 모두 유지(래퍼 형태) + 새 메서드명 병행 제공
 * - 읽기 기본(readOnly=true), 변경 메서드만 @Transactional
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceContactService {

    private final ServiceContactRepository serviceContactRepository;

    /* =========================
       조회 계열 (새 이름)
       ========================= */

    /** 이메일 기준 Optional 조회 */
    public Optional<ServiceContact> findByEmail(String email) {
        return serviceContactRepository.findByEmail(email);
    }

    /** 이메일 기준 강제 조회(없으면 예외) */
    public ServiceContact getByEmailOrThrow(String email) {
        return serviceContactRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("담당자 정보를 찾을 수 없습니다. email=" + email));
    }

    /** 고객 ID 기준 선행 1건 조회(없으면 null) */
    public ServiceContact getFirstByCustomerIdOrNull(String customerId) {
        return serviceContactRepository.findFirstByCustomer_CustomerId(customerId).orElse(null);
    }

    /** 고객 ID + username으로 담당자 목록 */
    public List<ServiceContact> getByCustomerAndUsername(String customerId, String username) {
        return serviceContactRepository.findByCustomer_CustomerIdAndUsername(customerId, username);
    }

    /** 고객 ID + 서비스 ID로 담당자 목록 */
    public List<ServiceContact> getByCustomerAndService(String customerId, Integer serviceId) {
        return serviceContactRepository.findByCustomer_CustomerIdAndServiceId(customerId, serviceId);
    }

    /** 서비스 ID + 이메일로 담당자 목록 */
    public List<ServiceContact> getByServiceIdAndEmail(Integer serviceId, String email) {
        return serviceContactRepository.findServiceContactByServiceIdAndEmail(serviceId, email);
    }

    /* =========================
       변경/저장 계열
       ========================= */

    /** 프로필(표시명/메모) 갱신 */
    @Transactional
    public ServiceContact updateProfile(String email, String username, String memo) {
        ServiceContact contact = getByEmailOrThrow(email);
        if (username != null) contact.setUsername(username.trim());
        contact.setMemo(memo != null ? memo.trim() : null);
        return serviceContactRepository.save(contact);
    }

    /** 비밀번호 해시 갱신(이미 인코딩된 값 전달) */
    @Transactional
    public void updatePasswordHash(String email, String encodedPasswordHash) {
        ServiceContact contact = getByEmailOrThrow(email);
        contact.setPasswordHash(encodedPasswordHash);
        serviceContactRepository.save(contact);
    }

    /** 임의 저장(신규/부분 업데이트 등) */
    @Transactional
    public ServiceContact save(ServiceContact contact) {
        return serviceContactRepository.save(contact);
    }

    /* =====================================================================
       🔁 아래부터는 "기존 코드 호환용" 메서드명 (구 이름 유지, 내부 위임)
       ===================================================================== */

    /** 과거: getByCustomerId */
    public ServiceContact getByCustomerId(String customerId) {
        return getFirstByCustomerIdOrNull(customerId);
    }

    /** 과거: getByEmail (예외 던지는 버전) */
    public ServiceContact getByEmail(String email) {
        return getByEmailOrThrow(email);
    }

    /** 과거: getServiceContactByCustomerAndUsername */
    public List<ServiceContact> getServiceContactByCustomerAndUsername(String customerId, String username) {
        return getByCustomerAndUsername(customerId, username);
    }

    /** 과거: getServiceContactByCustomerAndService */
    public List<ServiceContact> getServiceContactByCustomerAndService(String customerId, Integer serviceId) {
        return getByCustomerAndService(customerId, serviceId);
    }

    /** 과거: getServiceContactByServiceIdAndEmail */
    public List<ServiceContact> getServiceContactByServiceIdAndEmail(Integer serviceId, String email) {
        return getByServiceIdAndEmail(serviceId, email);
    }
}
