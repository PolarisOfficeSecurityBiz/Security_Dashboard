package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.ServiceContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceContactRepository extends JpaRepository<ServiceContact, Integer> {

    /** 특정 고객-서비스 조합의 연락 담당자 목록 (최신순) */
    List<ServiceContact> findByCustomerIdAndServiceIdOrderByCreateAtDesc(String customerId, Integer serviceId);

    /** 고객사 삭제 시 해당 고객의 모든 담당자 삭제 */
    long deleteByCustomerId(String customerId);

    /** 특정 고객 + 특정 서비스 담당자만 삭제 */
    long deleteByCustomerIdAndServiceId(String customerId, Integer serviceId);

    /** 고객-서비스별 담당자 목록 */
    List<ServiceContact> findByCustomerIdAndServiceId(String customerId, Integer serviceId);

    /** 서비스별 전체 담당자 목록 */
    List<ServiceContact> findByServiceId(Integer serviceId);

    /** 이메일로 담당자 조회 */
    Optional<ServiceContact> findByEmail(String email);

    /** 고객별 첫 담당자 조회 */
    Optional<ServiceContact> findFirstByCustomerId(String customerId);

    /** ✅ username(로그인 계정명)으로 담당자 조회 */
    Optional<ServiceContact> findByUsername(String username);
}
