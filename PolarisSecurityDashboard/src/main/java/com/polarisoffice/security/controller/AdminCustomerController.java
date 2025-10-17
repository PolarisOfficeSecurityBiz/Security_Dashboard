package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.Sort;

// ▼ 라이선스
import com.polarisoffice.security.model.License;
import com.polarisoffice.security.service.LicenseService;
import com.polarisoffice.security.service.LicenseHistoryService;

// ▼ 서비스 담당자
import com.polarisoffice.security.repository.ServiceContactRepository;

@RequiredArgsConstructor
public class AdminCustomerController {

    // -----------------------
    // ✅ 1) JSON (Ajax API)
    // -----------------------
    @RestController
    @RequestMapping("/admin/api/customers")
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
                m.put("serviceCount", svc != null ? svc.size() : 0);

                String serviceNames = (svc != null && !svc.isEmpty())
                        ? String.join(",", svc.stream().map(com.polarisoffice.security.model.Service::getServiceName).toList())
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
            // 필요 시 실제 통계 로직으로 교체
            m.put("vguard", 0);
            m.put("secuone", 0);
            return m;
        }
    }

    // -----------------------
    // ✅ 2) View (Thymeleaf 페이지)
    // -----------------------
    @Controller
    @RequestMapping("/admin/customers")
    @RequiredArgsConstructor
    static class View {

        private final CustomerRepository customerRepository;
        private final ServiceService serviceService;

        // 라이선스
        private final LicenseService licenseService;
        private final LicenseHistoryService licenseHistoryService;

        // 담당자
        private final ServiceContactRepository contactRepository;

        /** 고객사 목록 페이지 (뷰 렌더 시 드롭다운 옵션 주입) */
        @GetMapping
        public String listPage(Model model) {
            model.addAttribute("allCustomers",
                customerRepository.findAll(Sort.by("customerName").ascending()));
            return "admin/customer/customers";
        }

        /** 고객사 생성 (모달 POST) */
        @PostMapping
        public String create(@RequestParam String customerName,
                             @RequestParam(required = false) String connectedCompany) {
            Customer c = new Customer();
            c.setCustomerId(UUID.randomUUID().toString());
            c.setCustomerName(customerName);
            if (connectedCompany != null && !connectedCompany.isBlank()) {
                customerRepository.findById(connectedCompany).ifPresent(c::setConnectedCompany);
            }
            c.setCreateAt(LocalDate.now());

            customerRepository.save(c);
            return "redirect:/admin/customers/" + c.getCustomerId();
        }

        /** 고객사 상세 페이지 */
        @GetMapping("/{id}")
        public String viewCustomerDetail(@PathVariable("id") String id, Model model) {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("고객사 정보를 찾을 수 없습니다."));

            String connectedCompanyName = customer.getConnectedCompany() != null
                    ? customer.getConnectedCompany().getCustomerName() : "—";

            var services = serviceService.getServicesByCustomerId(customer.getCustomerId());

            model.addAttribute("customer", customer);
            model.addAttribute("connectedCompanyName", connectedCompanyName);
            model.addAttribute("services", services);

            return "admin/customer/customer_detail";
        }

        /** 서비스 상세 페이지 */
        @GetMapping("/{customerId}/services/{serviceId}")
        public String viewServiceDetail(@PathVariable String customerId,
                                        @PathVariable Integer serviceId,
                                        Model model) {

            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("고객사 정보를 찾을 수 없습니다."));

            // 소속 검증 포함 조회
            com.polarisoffice.security.model.Service svc =
                    serviceService.getByIdAndCustomer(serviceId, customerId);

            // 담당자 목록 (템플릿에서 contacts 사용)
            var contacts = contactRepository
                    .findByCustomer_CustomerIdAndServiceIdOrderByCreateAtDesc(customerId, serviceId);

            model.addAttribute("customer", customer);
            model.addAttribute("service", svc);
            model.addAttribute("contacts", contacts);

            return "admin/customer/service_detail";
        }

        // =========================================================
        // 🟢 라이선스 발급 (service_detail에서 '발급하기')
        //   JS 폼 필드명: expiryDate / usageLimit / licenseType / licenseVersion
        //   POST /admin/customers/{customerId}/services/{serviceId}/license
        // =========================================================
        @PostMapping("/{customerId}/services/{serviceId}/license")
        public String issueLicense(@PathVariable String customerId,
                                   @PathVariable Integer serviceId,
                                   @RequestParam("expiryDate") String expiryDate,
                                   @RequestParam(value = "usageLimit", required = false) Integer usageLimit,
                                   @RequestParam("licenseType") String licenseType,
                                   @RequestParam("licenseVersion") String licenseVersion) {

            // 검증
            customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("고객사 정보를 찾을 수 없습니다."));
            com.polarisoffice.security.model.Service svc =
                    serviceService.getByIdAndCustomer(serviceId, customerId);

            // 발급 처리 (서비스-라이선스 1:1, issueForService 내부에서 service 매핑/저장)
            licenseService.issueForService(svc, expiryDate, usageLimit, licenseType, licenseVersion);

            return "redirect:/admin/customers/" + customerId + "/services/" + serviceId + "/license";
        }

        // =========================================================
        // 🟢 라이선스 상세 (서비스 → 라이선스 페이지)
        //   없으면 서비스 상세로 리다이렉트(+ ?open=license 로 모달 오픈 힌트)
        //   GET /admin/customers/{customerId}/services/{serviceId}/license
        // =========================================================
        @GetMapping("/{customerId}/services/{serviceId}/license")
        public String viewLicenseDetail(@PathVariable String customerId,
                                        @PathVariable Integer serviceId,
                                        Model model) {

            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("고객사 정보를 찾을 수 없습니다."));

            com.polarisoffice.security.model.Service svc =
                    serviceService.getByIdAndCustomer(serviceId, customerId);

            return licenseService.findByServiceId(serviceId)
                    .map(lic -> {
                        model.addAttribute("customer", customer);
                        model.addAttribute("service", svc);
                        model.addAttribute("license", lic);
                        model.addAttribute("histories",
                                licenseHistoryService.findByLicenseIdOrderByCreateAtDesc(lic.getLicenseId()));
                        return "admin/customer/license_detail";
                    })
                    .orElse("redirect:/admin/customers/" + customerId + "/services/" + serviceId + "?open=license");
        }
    }
}
