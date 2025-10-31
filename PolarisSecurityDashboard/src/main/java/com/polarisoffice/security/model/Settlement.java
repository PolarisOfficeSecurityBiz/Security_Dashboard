package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "settlement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDate date;  // 정산 날짜
    private Long amount;     // 정산 금액
    private String service;  // 제휴사명 (선택적)
    private String fileId;   // 정산파일 아이디 (옵션)
}