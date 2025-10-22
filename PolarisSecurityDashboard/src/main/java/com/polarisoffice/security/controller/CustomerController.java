package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.service.CustomerInfoService;
import com.polarisoffice.security.service.ServiceContactService;
import com.polarisoffice.security.service.ServiceService;
import com.polarisoffice.security.service.VGuardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerInfoService customerInfoService;

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
        // ✅ 로그인 사용자명 안전 추출
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof com.polarisoffice.security.auth.CustomUserDetails customUser) {
            username = customUser.getUsername();
            logger.info("Logged in as CustomUserDetails: {}", username);
        } else if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            username = springUser.getUsername();
            logger.info("Logged in as Spring User: {}", username);
        } else {
            username = String.valueOf(principal);
            logger.info("Logged in as unknown principal: {}", username);
        }

        // ✅ 로그인 사용자 기준 고객 찾기
        Customer customer = customerInfoService.getCustomerInfo(username);
        if (customer == null) {
            logger.error("Customer not found for username: {}", username);
            throw new IllegalArgumentException("고객 정보를 찾을 수 없습니다.");
        }

        // ✅ 연결사명
        String connectedCompanyName = "-";
        Customer connectedCompany = customer.getConnectedCompany();
        if (connectedCompany != null) {
            connectedCompanyName = connectedCompany.getCustomerName();
            logger.info("Connected company name: {}", connectedCompanyName);
        } else {
            logger.info("No connected company found for customer: {}", username);
        }

        // ✅ 내가 담당인 서비스 목록만 가져오기: ServiceContact → serviceId 목록 → Service 목록
        List<ServiceContact> serviceContacts =
                contactService.getServiceContactByCustomerAndUsername(customer.getCustomerId(), username);

        logger.info("Service contacts found for username {}: {}", username, serviceContacts.size());

        // serviceId 중복 제거
        List<Integer> serviceIds = serviceContacts.stream()
                .map(ServiceContact::getServiceId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        logger.info("Service IDs after distinct: {}", serviceIds);

        // 서비스 엔티티들 조회 (빈 경우 대비)
        List<Service> services = serviceIds.isEmpty()
                ? Collections.emptyList()
                : serviceService.getByIds(serviceIds);

        logger.info("Found services for customer {}: {}", username, services.size());

        // ✅ 모델에 주입 (템플릿 호환 유지: services 사용)
        model.addAttribute("customer", customer);
        model.addAttribute("connectedCompanyName", connectedCompanyName);
        model.addAttribute("services", services);                 // 기존 템플릿 그대로 사용 가능
        model.addAttribute("serviceContacts", serviceContacts);   // 필요 시 사용

        return "customer/company";
    }
}
