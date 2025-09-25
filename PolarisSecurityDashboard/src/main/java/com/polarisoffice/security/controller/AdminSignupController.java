package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.SignupRequest;
import com.polarisoffice.security.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminSignupController {

    private final UserService userService;

    public AdminSignupController(UserService userService) {
        this.userService = userService;
    }

    /** 관리자 회원가입 폼 */
    @GetMapping("/admin/signup")
    public String adminSignupPage() {
        return "admin_signup"; // templates/admin/signup.html
    }

    /** 관리자 회원가입 처리 */
    @PostMapping("/admin/signup")
    public String adminSignup(@ModelAttribute SignupRequest req,
                              @RequestParam(name = "password2", required = false) String password2,
                              RedirectAttributes ra) {
        if (password2 != null && !req.getPassword().equals(password2)) {
            ra.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "redirect:/admin/signup";
        }
        try {
            userService.signup(req); // 내부에서 중복/포맷 검증 + 암호화 + 저장
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/signup";
        }
        ra.addFlashAttribute("message", "관리자 계정이 생성되었습니다. 로그인해 주세요.");
        return "redirect:/login"; // ★ 성공 후 로그인 폼으로 이동
    }
}
