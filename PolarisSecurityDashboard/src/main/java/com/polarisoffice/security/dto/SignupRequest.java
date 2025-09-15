package com.polarisoffice.security.dto;

public class SignupRequest {
	private String email;
    private String password;
    private String username;
    private String role; // "ADMIN" 또는 "CUSTOMER"

    public SignupRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}