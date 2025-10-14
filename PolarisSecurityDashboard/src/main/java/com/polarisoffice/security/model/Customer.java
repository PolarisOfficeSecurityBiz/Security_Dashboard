package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "customer_id", length = 64)
    @Comment("고객사 PK")
    private String customerId;

    @Column(name = "customer_name", length = 200, nullable = false)
    @Comment("고객사명")
    private String customerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_company", referencedColumnName = "customer_id")
    @Comment("연결된 판매사/파트너(예: 총판, 리셀러)")
    private Customer connectedCompany;

    @Column(name = "create_at")
    @Comment("생성일")
    private LocalDate createAt;


}