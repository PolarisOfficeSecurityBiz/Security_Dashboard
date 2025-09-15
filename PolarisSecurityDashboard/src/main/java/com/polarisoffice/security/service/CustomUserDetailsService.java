package com.polarisoffice.security.service;

import com.polarisoffice.security.model.User;
import com.polarisoffice.security.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository){ this.userRepository = userRepository; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.polarisoffice.security.model.User u = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        // DB에는 "ADMIN" 또는 "CUSTOMER"로 저장해 두고…
        String roleWithPrefix = "ROLE_" + u.getRole(); // → "ROLE_ADMIN"/"ROLE_CUSTOMER"
        return new org.springframework.security.core.userdetails.User(
            u.getEmail(), u.getPassword(), List.of(new SimpleGrantedAuthority(roleWithPrefix))
        );
    }
}