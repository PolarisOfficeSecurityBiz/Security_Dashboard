package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.LogEntry;
import com.polarisoffice.security.model.LogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    /**
     * 🔹 최근 로그 조회 (기존 그대로 유지)
     */
    @Query("""
        select l
          from LogEntry l
         where l.createdAt >= :from
           and (:type is null or l.logType = :type)
           and (:domain is null or lower(l.domain) like lower(concat('%', :domain, '%')))
         order by l.createdAt desc
    """)
    List<LogEntry> findRecent(
            @Param("from") LocalDateTime from,
            @Param("type") LogType type,
            @Param("domain") String domainLike
    );

    /**
     * 🔹 도메인 + 기간으로 로그 개수 조회 (스프링 자동 쿼리 메서드)
     *  예) logEntryRepo.countByDomainAndCreatedAtBetween("secuone.com", start, end)
     */
    int countByDomainAndCreatedAtBetween(
            String domain,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * 🔹 올해 월별 유입 현황 (월별 count)
     */
    @Query(value = """
        select month(l.created_at) as month,
               count(*) as cnt
          from log_entry l
         where lower(l.domain) = lower(:domain)
           and year(l.created_at) = :year
         group by month(l.created_at)
         order by month(l.created_at)
    """, nativeQuery = true)
    List<Object[]> countMonthlyJoinByYear(
            @Param("domain") String domain,
            @Param("year") int year
    );
}
