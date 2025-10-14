package com.polarisoffice.security.auth;

import com.polarisoffice.security.model.User;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.UserRepository;
import com.polarisoffice.security.repository.ServiceContactRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ServiceContactRepository serviceContactRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    ServiceContactRepository serviceContactRepository) {
        this.userRepository = userRepository;
        this.serviceContactRepository = serviceContactRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1️⃣ 관리자
        User admin = userRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            String authority = admin.getRole().startsWith("ROLE_")
                    ? admin.getRole() : "ROLE_" + admin.getRole();
            return org.springframework.security.core.userdetails.User
                    .withUsername(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities(authority)
                    .build();
        }

        // 2️⃣ 고객
        ServiceContact contact = serviceContactRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return new CustomUserDetails(
                contact,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

}