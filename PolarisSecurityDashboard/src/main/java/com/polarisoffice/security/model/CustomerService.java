package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_service")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 고객사와의 관계
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;  // 서비스 이름 (예: "V-Guard", "SecuOne")
    
    
    @Column(name = "license_id")  // 여기에 실제 licenseId 필드를 추가해야 합니다
    private String licenseId;     // 라이선스 키
}
