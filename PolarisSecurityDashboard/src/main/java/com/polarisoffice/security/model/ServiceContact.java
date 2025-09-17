package com.polarisoffice.security.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import org.hibernate.annotations.Comment;

@Entity
@Table(name = "servicecontacts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor 
@Builder
public class ServiceContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("연락처 PK(INT)")
    private Integer id;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "passwordHash", length = 255)
    private String passwordHash;

    @Column(name = "role", length = 50)
    private String role;

    @Column(name = "createAt")
    private LocalDate createAt;

    @Column(name = "memo", length = 500)
    private String memo;

    @Column(name = "customer_id", length = 64, nullable = false)
    private String customerId;

    @Column(name = "service_id", nullable = false)
    private Integer serviceId;
}