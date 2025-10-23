package com.polarisoffice.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.polarisoffice.security.model.SdkHistory;

@Repository
public interface SdkHistoryRepository extends JpaRepository<SdkHistory, Long> {
    List<SdkHistory> findBySdkIdOrderByCreatedAtDesc(Long sdkId);
}
