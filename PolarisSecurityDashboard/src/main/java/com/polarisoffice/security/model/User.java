package com.polarisoffice.security.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // unique는 위 uniqueConstraints로 설정
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * ADMIN / CUSTOMER 저장 (DB에는 접두어 없이 저장)
     * Spring Security로 권한 부여 시엔 "ROLE_" 접두어를 붙여 사용합니다.
     */
    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false, length = 100)
    private String username;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(String email, String password, String role, String username) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.username = username;
    }

    // getters/setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}