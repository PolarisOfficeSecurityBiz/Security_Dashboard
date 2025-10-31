package com.polarisoffice.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.polarisoffice.security.model.Settlement;

import java.util.List;
import java.util.Map;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /**
     * ✅ 특정 고객사의 특정 월 정산 금액 합계
     */
    @Query("""
        SELECT COALESCE(SUM(s.amount), 0)
        FROM Settlement s
        WHERE s.customer.id = :customerId
          AND MONTH(s.date) = :month
    """)
    long sumAmountByCustomerAndMonth(@Param("customerId") String customerId,
                                     @Param("month") int month);

    /**
     * ✅ 올해 정산 내역 (월별 금액 합계)
     * - 결과 예: [{month:1, amount:1200000}, {month:2, amount:900000}, ...]
     */
    @Query(value = """
        SELECT MONTH(s.date) AS month,
               COALESCE(SUM(s.amount), 0) AS amount
        FROM settlement s
        WHERE s.customer_id = :customerId
          AND YEAR(s.date) = YEAR(CURDATE())
        GROUP BY MONTH(s.date)
        ORDER BY MONTH(s.date)
    """, nativeQuery = true)
    List<Map<String, Object>> findByCustomerYearly(@Param("customerId") String customerId);
}
