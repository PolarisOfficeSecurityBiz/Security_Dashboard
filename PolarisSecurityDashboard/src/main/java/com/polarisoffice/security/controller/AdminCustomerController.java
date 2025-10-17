package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.Sort;

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

                List<com.polarisoffice.security.model.Service> svc =
                        serviceService.getServicesByCustomerId(c.getCustomerId());
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
            m.put("vguard", 0);
            m.put("secuone", 0);
            return m;
        }

        // ❌ (삭제) 뷰를 반환하던 listPage는 @RestController에 두면 안 됩니다.
        // @GetMapping
        // public String listPage(Model model) { ... }
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

        /** 고객사 목록 페이지 (뷰 렌더 시 드롭다운 옵션 주입) */
        @GetMapping
        public String listPage(Model model) {
            model.addAttribute("allCustomers",
                customerRepository.findAll(org.springframework.data.domain.Sort.by("customerName").ascending()));
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

            List<com.polarisoffice.security.model.Service> services =
                    serviceService.getServicesByCustomerId(customer.getCustomerId());

            model.addAttribute("customer", customer);
            model.addAttribute("connectedCompanyName", connectedCompanyName);
            model.addAttribute("services", services);

            return "admin/customer/customer_detail";
        }
    }
}
