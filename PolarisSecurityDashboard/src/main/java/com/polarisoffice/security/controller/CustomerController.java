package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.service.CustomerInfoService;
import com.polarisoffice.security.service.ServiceContactService;
import com.polarisoffice.security.service.ServiceService;
import com.polarisoffice.security.service.VGuardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CustomerController {

    @Autowired
    private CustomerInfoService customerInfoService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ServiceContactService contactService;
    
    @Autowired
    private VGuardService vguardService;
    

    /**
     * 고객사 정보 페이지
     */
    @GetMapping("/customer/company")
    public String companyPage(Model model, Authentication authentication) {
        // ✅ 로그인 사용자 정보 안전하게 추출
        Object principal = authentication.getPrincipal();
        String username;

        // CustomUserDetails → getUsername()
        if (principal instanceof com.polarisoffice.security.auth.CustomUserDetails customUser) {
            username = customUser.getUsername();
        }
        // 기본 Spring Security User → getUsername()
        else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            username = springUser.getUsername();
        }
        // 문자열 등 다른 타입인 경우
        else {
            username = principal.toString();
        }

        // 🔹 로그인한 담당자 기준 고객사 조회
        Customer customer = customerInfoService.getCustomerInfo(username);
        if (customer == null) {
            throw new IllegalArgumentException("고객 정보를 찾을 수 없습니다.");
        }

        // 🔹 연결된 회사명 조회 (connected_company = FK)
        String connectedCompanyName = "-";
        Customer connectedCompany = customer.getConnectedCompany();
        if (connectedCompany != null) {
            connectedCompanyName = connectedCompany.getCustomerName();
        }

        // 🔹 담당 서비스 목록 조회
        List<Service> services = serviceService.getServicesByCustomerId(customer.getCustomerId());

        // 🔹 등록 담당자 정보 조회
        ServiceContact contact = contactService.getByCustomerId(customer.getCustomerId());

        // 🔹 Thymeleaf로 전달
        model.addAttribute("customer", customer);
        model.addAttribute("connectedCompanyName", connectedCompanyName);
        model.addAttribute("services", services);
        model.addAttribute("contact", contact);

        return "customer/company";
    }
    

    
}
