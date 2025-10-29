package com.polarisoffice.security.controller;

import com.polarisoffice.security.auth.CustomUserDetails;
import com.polarisoffice.security.dto.ServiceUnlessDto;
import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.model.ServiceContact;
import com.polarisoffice.security.repository.ServiceRepository;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.ServiceContactRepository;
import com.polarisoffice.security.service.CustomerInfoService;
import com.polarisoffice.security.service.ServiceContactService;
import com.polarisoffice.security.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final ServiceContactRepository contactRepository;

    private static final Logger logger = LoggerFactory.getLogger(CustomerLicenseController.class);

    @GetMapping("/customer/license")
    public String licensePage(Model model, Authentication authentication) {
        String email = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        ServiceContact contact = contactService.getByEmail(email);

        // 고객사 정보 조회
        Customer customer = contact.getCustomer();
        if (customer == null) {
            logger.error("고객 정보를 찾을 수 없습니다. email=" + email);
            throw new IllegalArgumentException("고객 정보를 찾을 수 없습니다. email=" + email);
        }

        // 주요 서비스 조회
        Service service = serviceService.getPrimaryService(customer.getCustomerId());

        // 연결된 고객사 이름 (없으면 "-"로 설정)
        String connectedCompanyName = "-";
        if (customer.getConnectedCompany() != null) {
            connectedCompanyName = customer.getConnectedCompany().getCustomerName();
        }

        // 라이선스 키 값 확인 로그 추가
        if (service != null) {
            logger.info("License Key: {}", service.getLicenseId());
        } else {
            logger.warn("No service found for customer: {}", customer.getCustomerId());
        }

        model.addAttribute("customer", customer);
        model.addAttribute("service", service);
        model.addAttribute("contact", contact);
        model.addAttribute("connectedCompanyName", connectedCompanyName);

        return "customer/license"; // "license.html"을 반환
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
        Service service = serviceService.getPrimaryService(customer.getCustomerId());

        // 라이선스 키가 비어있지 않으면 다운로드 제공
        String content = (service != null && service.getLicenseId() != null)
                ? String.valueOf(service.getLicenseId())
                : "라이선스 키 없음";

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "vguard.key");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    // 서비스가 발급되지 않은 리스트 가져오기
    public List<ServiceUnlessDto> getUnissuedServices() {
        List<Object[]> results = serviceRepository.findUnissuedServices();
        List<ServiceUnlessDto> serviceDTOs = new ArrayList();

        // 결과 리스트를 ServiceUnlessDTO 객체로 변환
        for (Object[] result : results) {
            Integer serviceId = (Integer) result[0];
            String serviceName = (String) result[1];
            String customerName = (String) result[2];
            String contactName = (String) result[3];
            LocalDate createAt = (result.length > 4) ? (LocalDate) result[4] : null;
            String customerId = (result.length > 5) ? (String) result[5] : null;

            // DTO 객체 생성
            ServiceUnlessDto dto = new ServiceUnlessDto(serviceId, serviceName, customerName, contactName, createAt, customerId);
            serviceDTOs.add(dto);
        }

        // 디버그 로그 출력 (서비스가 반환되는지 확인)
        for (ServiceUnlessDto serviceDto : serviceDTOs) {
            System.out.println("Service DTO: " + serviceDto.toString()); // DTO의 모든 정보를 출력
        }

        return serviceDTOs;
    }

    @GetMapping("/admin/license")
    public String getUnissuedServices(Model model) {
        List<ServiceUnlessDto> services = getUnissuedServices();
        model.addAttribute("services", services);  // 데이터를 Thymeleaf로 전달
        return "admin/license/admin_license";
    }
}
