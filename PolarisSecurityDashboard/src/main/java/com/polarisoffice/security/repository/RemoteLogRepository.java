package com.polarisoffice.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.polarisoffice.security.model.RemoteLog;

public interface RemoteLogRepository extends JpaRepository<RemoteLog, Long> {}

