package com.polarisoffice.security.controller;

import com.polarisoffice.security.auth.CustomUserDetails;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.service.ServiceContactService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 마이페이지 (프로필 / 비밀번호 변경)
 *
 * - GET  /customer/mypage            : 화면 렌더링 (내 정보 표시)
 * - POST /customer/mypage            : 이름 저장 (memo는 VM엔 있으나 화면 미표시)
 * - POST /customer/mypage/password   : 비밀번호 변경
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/mypage")
public class CustomerMyPageController {

    private final ServiceContactService contactService;
    private final PasswordEncoder passwordEncoder;

    /** 마이페이지 화면 */
    @GetMapping
    public String mypage(Model model, Authentication authentication) {
        String email = currentUserEmail(authentication);
        ServiceContact contact = null;
        try {
            contact = contactService.getByEmailOrThrow(email);
        } catch (Exception ignored) {}

        ProfileVM vm = new ProfileVM();
        vm.setEmail(email);
        vm.setUsername(contact != null ? nvl(contact.getUsername()) : "");
        vm.setMemo(contact != null ? nvl(contact.getMemo()) : "");

        model.addAttribute("path", "/customer/mypage"); // 좌측 네비 active용(선택)
        model.addAttribute("profile", vm);
        return "customer/mypage";
    }

    /** 프로필 저장 (username) */
    @PostMapping
    public String saveProfile(@ModelAttribute("profile") ProfileVM form,
                              BindingResult binding,
                              Authentication authentication,
                              RedirectAttributes ra) {
        String email = currentUserEmail(authentication);

        if (!StringUtils.hasText(form.getUsername())) {
            ra.addFlashAttribute("errorMsg", "이름을 입력해주세요.");
            return "redirect:/customer/mypage";
        }
        if (form.getUsername().length() > 60) {
            ra.addFlashAttribute("errorMsg", "이름은 60자 이하로 입력해주세요.");
            return "redirect:/customer/mypage";
        }

        try {
            // memo는 화면에 노출하지 않지만, 기존 값 유지/갱신 로직이 필요하면 서비스 레이어에서 보관하거나 빈 문자열 처리
            contactService.updateProfile(email, form.getUsername(), form.getMemo());
            ra.addFlashAttribute("successMsg", "프로필이 저장되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "프로필 저장 중 오류가 발생했습니다.");
        }
        return "redirect:/customer/mypage";
    }

    /** 비밀번호 변경 */
    @PostMapping("/password")
    public String changePassword(@ModelAttribute PasswordForm form,
                                 BindingResult binding,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        String email = currentUserEmail(authentication);

        // 기본 검증
        if (!StringUtils.hasText(form.getCurrentPassword()) ||
            !StringUtils.hasText(form.getNewPassword()) ||
            !StringUtils.hasText(form.getConfirmPassword())) {
            ra.addFlashAttribute("errorMsg", "모든 비밀번호 필드를 입력해주세요.");
            return "redirect:/customer/mypage";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            ra.addFlashAttribute("errorMsg", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "redirect:/customer/mypage";
        }
        if (form.getNewPassword().length() < 8) {
            ra.addFlashAttribute("errorMsg", "새 비밀번호는 8자 이상이어야 합니다.");
            return "redirect:/customer/mypage";
        }
        if (form.getNewPassword().equals(form.getCurrentPassword())) {
            ra.addFlashAttribute("errorMsg", "현재 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
            return "redirect:/customer/mypage";
        }

        try {
            // 현재 비밀번호 검증
            ServiceContact contact = contactService.getByEmailOrThrow(email);
            String encoded = contact.getPasswordHash();
            if (encoded == null || !passwordEncoder.matches(form.getCurrentPassword(), encoded)) {
                ra.addFlashAttribute("errorMsg", "현재 비밀번호가 올바르지 않습니다.");
                return "redirect:/customer/mypage";
            }

            // 새 비밀번호 저장
            String encodedNew = passwordEncoder.encode(form.getNewPassword());
            contactService.updatePasswordHash(email, encodedNew);

            ra.addFlashAttribute("successMsg", "비밀번호가 변경되었습니다. 다음 로그인부터 적용됩니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "비밀번호 변경 중 오류가 발생했습니다.");
        }
        return "redirect:/customer/mypage";
    }

    /* ------------ 내부 유틸 & 폼 DTO ------------ */

    private String currentUserEmail(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;
        Object p = authentication.getPrincipal();
        if (p instanceof CustomUserDetails cud) return cud.getUsername();
        return authentication.getName();
    }

    private String nvl(String s) { return (s == null ? "" : s); }

    @Data
    public static class ProfileVM {
        private String email;               // readonly
        @NotBlank @Size(max = 60)
        private String username;            // 이름(표시명)
        private String memo;                // 메모 (화면 미표시)
    }

    @Data
    public static class PasswordForm {
        @NotBlank private String currentPassword;
        @NotBlank private String newPassword;
        @NotBlank private String confirmPassword;
    }
}
