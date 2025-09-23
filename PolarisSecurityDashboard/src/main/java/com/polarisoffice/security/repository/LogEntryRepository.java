package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {}
