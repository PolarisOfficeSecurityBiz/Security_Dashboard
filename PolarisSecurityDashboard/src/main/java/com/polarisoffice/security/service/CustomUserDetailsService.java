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
            .orElseThrow(() -> new UsernameNotFoundException(email));

        // DB role이 ADMIN/CUSTOMER 로 저장되어 있다면 접두사 부여
        String authority = u.getRole().startsWith("ROLE_") ? u.getRole() : "ROLE_" + u.getRole();

        return org.springframework.security.core.userdetails.User
            .withUsername(u.getEmail())      // ← 시큐리티의 username은 email로 사용
            .password(u.getPassword())       // ← BCrypt 해시 여야 함
            .authorities(authority)
            .build();
    }
}