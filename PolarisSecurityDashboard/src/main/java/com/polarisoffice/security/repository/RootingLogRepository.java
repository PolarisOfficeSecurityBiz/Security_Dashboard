package com.polarisoffice.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.polarisoffice.security.model.RootingLog;

public interface RootingLogRepository extends JpaRepository<RootingLog, Long> {}
