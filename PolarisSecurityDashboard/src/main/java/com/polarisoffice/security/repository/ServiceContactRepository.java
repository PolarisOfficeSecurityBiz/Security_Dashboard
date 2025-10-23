package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.ServiceContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /** 고객별 가장 최신 담당자 조회 */
    Optional<ServiceContact> findTopByCustomer_CustomerIdOrderByCreateAtDesc(String customerId);

    /** 특정 고객-서비스별 담당자 조회 */
    Optional<ServiceContact> findTopByCustomer_CustomerIdAndServiceIdOrderByCreateAtDesc(String customerId, Integer serviceId);

    /** 서비스 담당자와 그에 연결된 고객 및 서비스 정보를 가져오는 쿼리 */
    @Query("SELECT sc FROM ServiceContact sc " +
           "JOIN FETCH sc.customer c " +
           "JOIN FETCH sc.service s " +
           "WHERE c.customerId = :customerId AND sc.email = :username")
    List<ServiceContact> findByCustomer_CustomerIdAndUsername(@Param("customerId") String customerId, @Param("username") String username);
    
    @Query("SELECT sc FROM ServiceContact sc " +
           "JOIN FETCH sc.customer c " +
           "JOIN FETCH sc.service s " +
           "WHERE sc.serviceId = :serviceId AND sc.email = :email")
    List<ServiceContact> findServiceContactByServiceIdAndEmail(@Param("serviceId") Integer serviceId, @Param("email") String email);
}
