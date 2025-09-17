package com.polarisoffice.security.model;

import java.time.LocalDate;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="service")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    @Comment("서비스 PK(INT)")
    private Integer serviceId;

    @Column(name = "service_name", length = 200, nullable = false)
    private String serviceName;

    @Column(name = "domain", length = 255)
    private String domain;

    @Column(name = "product_type", length = 100)
    private String productType;

    @Column(name = "cpiValue")
    private Integer cpiValue;

    @Column(name = "rsRate")
    private Double rsRate;

    @Column(name = "createAt")
    private LocalDate createAt;

    @Column(name = "updateAt")
    private LocalDate updateAt;

    @Column(name = "customer_id", length = 64, nullable = false)
    @Comment("Customer FK (varchar64)")
    private String customerId;

    @Column(name = "license_id")
    private Integer licenseId; // 필요 시 다른 테이블 FK
}
