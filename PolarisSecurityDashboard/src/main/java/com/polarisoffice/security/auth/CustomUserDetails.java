package com.polarisoffice.security.auth;

import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final String displayName;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(ServiceContact contact, Collection<? extends GrantedAuthority> authorities) {
        this.email = contact.getEmail();
        this.password = contact.getPasswordHash();
        this.displayName = contact.getUsername(); // 👉 DB username이 “이보연”
        this.authorities = authorities;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}