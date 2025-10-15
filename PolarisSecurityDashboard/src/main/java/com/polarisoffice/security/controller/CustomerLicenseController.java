package com.polarisoffice.security.controller;

import com.polarisoffice.security.auth.CustomUserDetails;
import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.service.CustomerInfoService;
import com.polarisoffice.security.service.ServiceContactService;
import com.polarisoffice.security.service.ServiceService;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CustomerLicenseController {

    private final CustomerInfoService customerInfoService;
    private final ServiceService serviceService;
    private final ServiceContactService contactService;

    @GetMapping("/customer/license")
    public String licensePage(Model model, Authentication authentication) {

        String email = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        ServiceContact contact = contactService.getByEmail(email);

        // ✅ 바로 customer 접근 가능
        Customer customer = contact.getCustomer();

        if (customer == null) {
            throw new IllegalArgumentException("고객 정보를 찾을 수 없습니다. email=" + email);
        }

        Service service = serviceService.getPrimaryService(customer.getCustomerId());

        String connectedCompanyName = "-";
        if (customer.getConnectedCompany() != null) {
            connectedCompanyName = customer.getConnectedCompany().getCustomerName();
        }

        model.addAttribute("customer", customer);
        model.addAttribute("service", service);
        model.addAttribute("contact", contact);
        model.addAttribute("connectedCompanyName", connectedCompanyName);

        return "customer/license";
    }
    
    @GetMapping("/customer/license/download")
    public ResponseEntity<byte[]> downloadLicenseKey(Authentication authentication) {
        String email;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUser) {
            email = customUser.getUsername();
        } else {
            email = authentication.getName();
        }

        // 로그인한 담당자 이메일로 ServiceContact 조회
        ServiceContact contact = contactService.getByEmail(email);
        // 고객사 정보 조회
        Customer customer = customerInfoService.getCustomerById(contact.getCustomer().getCustomerId());
        // 고객의 주요 서비스 조회
        com.polarisoffice.security.model.Service service =
                serviceService.getPrimaryService(customer.getCustomerId());

        // 파일 내용은 라이선스 키 숫자만
        String content = String.valueOf(service.getLicenseId());

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "vguard.key");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

}
