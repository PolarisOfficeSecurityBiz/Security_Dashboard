package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Integer> {

    /** ✅ 서비스 ID로 라이선스 조회 (1:1 매핑 기준) */
    Optional<License> findByService_ServiceId(Integer serviceId);

    /** ✅ 라이선스 키로 조회 */
    Optional<License> findByLicenseKey(String licenseKey);

    /* ==========================================================
       🧩 추가 — 대시보드용 (활성 라이선스, 상태 리스트)
    ========================================================== */

    /** 🔹 특정 고객사에 속한 활성 라이선스 목록 */
    @Query("""
        select l
          from License l
         where l.service.customer.customerId = :customerId
           and (l.expireDate is null or l.expireDate >= :today)
         order by l.expireDate asc
    """)
    List<License> findActiveByCustomer(
            @Param("customerId") String customerId,   // 🔸 Integer → String 변경
            @Param("today") LocalDate today
    );

    /** 🔹 특정 고객사 활성 라이선스 개수 */
    @Query("""
        select count(l)
          from License l
         where l.service.customer.customerId = :customerId
           and (l.expireDate is null or l.expireDate >= :today)
    """)
    int countActiveByCustomer(
            @Param("customerId") String customerId,   // 🔸 Integer → String 변경
            @Param("today") LocalDate today
    );
}
