package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.LogEntry;
import com.polarisoffice.security.model.LogType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
	
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
}
