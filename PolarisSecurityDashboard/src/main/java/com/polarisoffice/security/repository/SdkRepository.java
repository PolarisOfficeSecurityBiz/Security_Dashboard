package com.polarisoffice.security.repository;

import com.polarisoffice.security.model.SdkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SdkRepository extends JpaRepository<SdkEntity, Long> {
}