package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    private String domain;
    private String requesterEmail;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
