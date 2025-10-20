package com.polarisoffice.security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.polarisoffice.security.model.Sdk;

@Repository
public interface SdkRepository extends JpaRepository<Sdk, Long> {
                                
	Optional<Sdk> findBySdkType(String sdkType);                                                                                                        
                                                                        
    // ✅ SDK 전체 중 최신순으로 정렬                                              
    List<Sdk> findAllByOrderByUploadedAtDesc();                         
                                                                        
    // ✅ SDK 유형별 최신 업로드 1건                                              
    Optional<Sdk> findTopBySdkTypeOrderByUploadedAtDesc(String sdkType);
}