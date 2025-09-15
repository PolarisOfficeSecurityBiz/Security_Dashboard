package com.polarisoffice.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.polarisoffice.security.dto.LoginRequest;
import com.polarisoffice.security.dto.SignupRequest;
import com.polarisoffice.security.dto.UserResponse;
import com.polarisoffice.security.model.User;
import com.polarisoffice.security.repository.UserRepository;
import org.springframework.util.StringUtils;

@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 인터페이스 타입으로 주입

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입: 이메일 중복 체크 → 비밀번호 암호화 → 저장
    public UserResponse signup(SignupRequest req) {
        if (!StringUtils.hasText(req.getEmail()) ||
            !StringUtils.hasText(req.getPassword()) ||
            !StringUtils.hasText(req.getUsername())) {
            throw new IllegalArgumentException("email/password/username 는 필수입니다.");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        String role = StringUtils.hasText(req.getRole()) ? req.getRole().toUpperCase() : "CUSTOMER";
        if (!role.equals("ADMIN") && !role.equals("CUSTOMER")) {
            throw new IllegalArgumentException("role 은 ADMIN 또는 CUSTOMER 이어야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(req.getPassword());

        User user = new User(req.getEmail(), encodedPassword, role, req.getUsername());
        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getRole());
    }

    // (선택) 수동 로그인 검증용 도우미
//    public boolean login(LoginRequest req) {
//        return userRepository.findByEmail(req.getEmail())
//                .map(u -> passwordEncoder.matches(req.getPassword(), u.getPassword()))
//                .orElse(false);
//    }
}