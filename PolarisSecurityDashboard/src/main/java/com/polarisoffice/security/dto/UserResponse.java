package com.polarisoffice.security.dto;

public class UserResponse {

	 private Long id;
	    private String email;
	    private String username;
	    private String role;

	    public UserResponse() {}

	    public UserResponse(Long id, String email, String username, String role) {
	        this.id = id;
	        this.email = email;
	        this.username = username;
	        this.role = role;
	    }

	    public Long getId() { return id; }
	    public String getEmail() { return email; }
	    public String getUsername() { return username; }
	    public String getRole() { return role; }

	    public void setId(Long id) { this.id = id; }
	    public void setEmail(String email) { this.email = email; }
	    public void setUsername(String username) { this.username = username; }
	    public void setRole(String role) { this.role = role; }
}