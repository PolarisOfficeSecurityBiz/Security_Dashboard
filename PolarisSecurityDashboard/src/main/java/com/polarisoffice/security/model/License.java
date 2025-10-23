// src/main/java/com/polarisoffice/security/model/License.java
package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "license",
       uniqueConstraints = {
         @UniqueConstraint(name="uk_license_key", columnNames = "license_key"),
         @UniqueConstraint(name="uk_service_id", columnNames = "service_id") // 서비스당 1개만 허용 시
       })
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Integer licenseId;       // PK

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // 서비스 : 라이선스 = 1:N도 가능
    // @OneToOne(optional = false, fetch = FetchType.LAZY) // 정말로 1:1이면 이걸로 바꾸세요
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;         // FK 연관

    @Column(name = "license_key", length = 255, nullable = false)
    private String licenseKey;       // 유니크

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "use_limit")
    private Integer useLimit;        // default 2

    @Column(name = "license_type", length = 20)
    private String licenseType;      // PROD/TEST

    @Column(name = "create_date")
    private LocalDateTime createDate;

}
