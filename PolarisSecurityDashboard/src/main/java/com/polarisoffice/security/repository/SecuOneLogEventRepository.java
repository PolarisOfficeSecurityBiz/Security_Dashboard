package com.polarisoffice.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.polarisoffice.security.model.SecuOneLogEvent;

@Repository
public interface SecuOneLogEventRepository extends JpaRepository<SecuOneLogEvent, Long>{

}
