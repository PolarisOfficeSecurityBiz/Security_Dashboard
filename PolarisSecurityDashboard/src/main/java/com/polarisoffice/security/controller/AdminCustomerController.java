package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.Sort;

// ▼ 라이선스
import com.polarisoffice.security.service.LicenseService;
import com.polarisoffice.security.service.LicenseHistoryService;

// ▼ 서비스 담당자
import com.polarisoffice.security.repository.ServiceContactRepository;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerRepository customerRepository;
    private final ServiceService serviceService;
    private final ServiceContactRepository contactRepository;

    // 고객사 목록 페이지
    @GetMapping
    public String listCustomers(Model model) {
        List<Customer> customers = customerRepository.findAll(Sort.by("customerName").ascending());
        model.addAttribute("customers", customers);
        return "admin/customer/customers";  // "customers.html"을 반환
    }

    // 고객사 상세 페이지
    @GetMapping("/{customerId}")
    public String viewCustomerDetail(@PathVariable String customerId, Model model) {
        // 고객사 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객사 정보를 찾을 수 없습니다."));
        
        // 고객사에 연결된 서비스 리스트 조회
        var services = serviceService.getServicesByCustomerId(customerId);

        model.addAttribute("customer", customer);
        model.addAttribute("services", services);

        // 고객사 상세 페이지를 반환
        return "admin/customer/customer_detail";  // 고객사 상세 페이지 (customer_detail.html)
    }

    // 서비스 상세 페이지
    @GetMapping("/{customerId}/services/{serviceId}")
    public String viewServiceDetail(@PathVariable String customerId, @PathVariable Integer serviceId, Model model) {
        if (customerId == null || customerId.isEmpty()) {
            return "redirect:/admin/customers";  // 리디렉션 처리
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객사 정보를 찾을 수 없습니다."));

        com.polarisoffice.security.model.Service service = serviceService.getByIdAndCustomer(serviceId, customerId);

        // 서비스가 없으면 리다이렉트
        if (service == null) {
            return "redirect:/admin/customers/" + customerId;
        }

        var contacts = contactRepository.findByCustomer_CustomerIdAndServiceIdOrderByCreateAtDesc(customerId, serviceId);

        model.addAttribute("customer", customer);
        model.addAttribute("service", service);
        model.addAttribute("contacts", contacts);

        return "admin/customer/service_detail";  // 서비스 상세 페이지
    }

    // -----------------------
    // ✅ 1) JSON (Ajax API)
    // -----------------------
    @RestController
    @RequestMapping("/admin/api/customers") // API 경로를 /admin/api/customers로 수정
    @RequiredArgsConstructor
    static class Api {

        private final CustomerRepository customerRepository;
        private final ServiceService serviceService;

        /** 고객사 목록 JSON */
        @GetMapping("/list")
        public List<Map<String, Object>> getCustomerList() {
            List<Customer> customers = customerRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Customer c : customers) {
                Map<String, Object> m = new HashMap<>();
                m.put("customerId", c.getCustomerId());
                m.put("customerName", c.getCustomerName());
                m.put("createAt", c.getCreateAt());

                String connected = (c.getConnectedCompany() != null)
                        ? c.getConnectedCompany().getCustomerName() : "—";
                m.put("connectedCompanyName", connected);

                var svc = serviceService.getServicesByCustomerId(c.getCustomerId());
                m.put("serviceCount", (svc != null) ? svc.size() : 0);

                String serviceNames = (svc != null && !svc.isEmpty())
                        ? String.join(",", svc.stream()
                                              .map(com.polarisoffice.security.model.Service::getServiceName)
                                              .toList())
                        : "";
                m.put("services", serviceNames);

                result.add(m);
            }
            return result;
        }

        /** 간단 KPI */
        @GetMapping("/metrics")
        public Map<String, Object> getMetrics() {
            Map<String, Object> m = new HashMap<>();
            m.put("total", customerRepository.count());
            m.put("vguard", 0);  // 실제 통계 로직으로 교체
            m.put("secuone", 0);
            return m;
        }
    }
}
