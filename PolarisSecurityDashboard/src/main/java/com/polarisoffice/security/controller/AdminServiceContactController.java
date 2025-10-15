package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.ServiceContactRepository;
import com.polarisoffice.security.repository.ServiceRepository;
import com.polarisoffice.security.service.MailService;
import com.polarisoffice.security.support.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/customers/{cid}/services/{sid}/contacts")
@RequiredArgsConstructor
public class AdminServiceContactController {

    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceContactRepository contactRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String list(@PathVariable String cid, @PathVariable Integer sid) {
        customerRepository.findById(cid).orElseThrow();
        serviceRepository.findByServiceIdAndCustomerId(sid, cid).orElseThrow();
        return "redirect:/admin/customers/{cid}/services/{sid}";
    }

    /** 담당자 생성 + 임시 비밀번호 발급/메일 발송 */
    @PostMapping
    public String create(@PathVariable String cid, @PathVariable Integer sid,
                         @RequestParam String username,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String role,
                         @RequestParam(required = false) String memo) {

        // 고객, 서비스 존재 검증
        Customer customer = customerRepository.findById(cid).orElseThrow();
        serviceRepository.findByServiceIdAndCustomerId(sid, cid).orElseThrow();

        // 임시 비밀번호 생성 + 암호화
        String tempPw = PasswordGenerator.generate(12);
        String hash = passwordEncoder.encode(tempPw);

        // 엔티티 생성
        ServiceContact sc = ServiceContact.builder()
                .customer(customer)        // ✅ 변경된 부분: customerId → customer
                .serviceId(sid)
                .username(username)
                .email((email != null && !email.isBlank()) ? email : null)
                .role((role != null && !role.isBlank()) ? role : null)
                .memo((memo != null && !memo.isBlank()) ? memo : null)
                .passwordHash(hash)
                .createAt(LocalDate.now())
                .build();

        contactRepository.save(sc);

        // 이메일 발송
        if (email != null && !email.isBlank()) {
            try {
                mailService.sendInitialPassword(email, username, tempPw);
            } catch (Exception e) {
                // 로그만 남기기
                System.err.println("초기 비밀번호 메일 발송 실패: " + email);
            }
        }

        return "redirect:/admin/customers/{cid}/services/{sid}";
    }

    /** 담당자 수정 */
    @PostMapping("/{contactId}/update")
    public String update(@PathVariable String cid, @PathVariable Integer sid, @PathVariable Integer contactId,
                         @RequestParam String username,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String role,
                         @RequestParam(required = false) String memo) {

        ServiceContact sc = contactRepository.findById(contactId).orElseThrow();

        sc.setUsername(username);
        sc.setEmail((email != null && !email.isBlank()) ? email : null);
        sc.setRole((role != null && !role.isBlank()) ? role : null);
        sc.setMemo((memo != null && !memo.isBlank()) ? memo : null);

        contactRepository.save(sc);

        return "redirect:/admin/customers/{cid}/services/{sid}";
    }

    /** 담당자 삭제 */
    @PostMapping("/{contactId}/delete")
    public String delete(@PathVariable String cid, @PathVariable Integer sid, @PathVariable Integer contactId) {
        contactRepository.deleteById(contactId);
        return "redirect:/admin/customers/{cid}/services/{sid}";
    }
}
