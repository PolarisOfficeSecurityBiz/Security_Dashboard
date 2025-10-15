package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceContactRepository extends JpaRepository<ServiceContact, Integer> {

    /** 특정 고객-서비스 조합의 연락 담당자 목록 (최신순) */
    List<ServiceContact> findByCustomer_CustomerIdAndServiceIdOrderByCreateAtDesc(String customerId, Integer serviceId);

    /** 고객사 삭제 시 해당 고객의 모든 담당자 삭제 */
    long deleteByCustomer_CustomerId(String customerId);

    /** 특정 고객 + 특정 서비스 담당자만 삭제 */
    long deleteByCustomer_CustomerIdAndServiceId(String customerId, Integer serviceId);

    /** 고객-서비스별 담당자 목록 */
    List<ServiceContact> findByCustomer_CustomerIdAndServiceId(String customerId, Integer serviceId);

    /** 서비스별 전체 담당자 목록 */
    List<ServiceContact> findByServiceId(Integer serviceId);

    /** 이메일로 담당자 조회 */
    Optional<ServiceContact> findByEmail(String email);

    /** 고객별 첫 담당자 조회 */
    Optional<ServiceContact> findFirstByCustomer_CustomerId(String customerId);
}
