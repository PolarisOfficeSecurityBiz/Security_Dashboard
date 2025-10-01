package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.MetricsDto;
import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.model.Service;
import com.polarisoffice.security.repository.CustomerRepository;
import com.polarisoffice.security.repository.ServiceRepository;
import com.polarisoffice.security.service.CompanyServiceService;
import com.polarisoffice.security.repository.ServiceContactRepository;
import com.polarisoffice.security.dto.CustomerRowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceContactRepository serviceContactRepository;
    private final CompanyServiceService compoanyService;

    /* 목록 */
    @GetMapping
    public String list(Model model) {
        // 전체 고객 조회 (셀렉트 옵션 & id->name 매핑에 함께 사용)
        List<Customer> all = customerRepository.findAll();

        // id -> name 매핑
        Map<String, String> idToName = all.stream()
                .collect(Collectors.toMap(Customer::getCustomerId, Customer::getCustomerName, (a, b) -> a));

        // 뷰 전용 DTO로 변환 (연결사명을 resolve)
        List<CustomerRowDto> rows = all.stream()
                .map(c -> new CustomerRowDto(
                        c.getCustomerId(),
                        c.getCustomerName(),
                        resolveConnectedCompanyName(c.getConnectedCompany(), idToName),
                        c.getCreateAt()
                ))
                .collect(Collectors.toList());

        model.addAttribute("customers", rows);          // 리스트 표시는 DTO
        model.addAttribute("connectedCompanies", all);  // 모달의 셀렉트 옵션은 원본 리스트
        MetricsDto metrics = compoanyService.getMetrics();
        model.addAttribute("metrics", metrics);
        return "admin/customer/customers";
    }

    private static String resolveConnectedCompanyName(String connectedCompanyId, Map<String, String> idToName) {
        if (connectedCompanyId == null || connectedCompanyId.isBlank()) return null;
        return idToName.getOrDefault(connectedCompanyId, connectedCompanyId); // 맵에 없으면 원값(id) fallback
    }

    /* 고객사 생성 */
    @PostMapping
    public String create(@RequestParam String customerName,
                         @RequestParam(required = false) String connectedCompany) {
        var c = new Customer();
        c.setCustomerId(UUID.randomUUID().toString());
        c.setCustomerName(customerName);
        c.setConnectedCompany((connectedCompany != null && !connectedCompany.isBlank()) ? connectedCompany : null);
        c.setCreateAt(LocalDate.now());
        customerRepository.save(c);
        return "redirect:/admin/customers";
    }

    /* 고객사 상세 (서비스 리스트 포함) */
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") String customerId, Model model) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객사를 찾을 수 없습니다: " + customerId));

        List<Service> services = serviceRepository.findByCustomerIdOrderByCreateAtDesc(customerId);

        model.addAttribute("customer", customer);
        model.addAttribute("services", services);
        return "admin/customer/customer_detail";
    }

    /* 고객사 수정/삭제 */
    @PostMapping("/{id}/update")
    public String updateCustomer(@PathVariable String id,
                                 @RequestParam String customerName,
                                 @RequestParam(required = false) String connectedCompany) {
        Customer c = customerRepository.findById(id).orElseThrow();
        c.setCustomerName(customerName);
        c.setConnectedCompany((connectedCompany != null && !connectedCompany.isBlank()) ? connectedCompany : null);
        customerRepository.save(c);
        return "redirect:/admin/customers/{id}";
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable String id) {
        // 서비스 연락처 → 서비스 → 고객사 순으로 삭제
        serviceContactRepository.deleteByCustomerId(id);
        serviceRepository.findByCustomerIdOrderByCreateAtDesc(id)
                .forEach(s -> serviceContactRepository.deleteByCustomerIdAndServiceId(id, s.getServiceId()));
        serviceRepository.deleteAll(serviceRepository.findByCustomerIdOrderByCreateAtDesc(id));
        customerRepository.deleteById(id);
        return "redirect:/admin/customers";
    }

    /* 서비스 생성/수정/삭제 */
    @PostMapping("/{id}/services")
    public String createService(@PathVariable String id,
                                @RequestParam String serviceName,
                                @RequestParam(required = false) String domain,
                                @RequestParam(required = false) String productType,
                                @RequestParam(required = false) Integer cpiValue,
                                @RequestParam(required = false) Double rsRate,
                                @RequestParam(required = false) Integer licenseId) {
        customerRepository.findById(id).orElseThrow(); // 존재 확인

        Service s = Service.builder()
                .customerId(id)
                .serviceName(serviceName)
                .domain(blankToNull(domain))
                .productType(blankToNull(productType))
                .cpiValue(cpiValue)
                .rsRate(rsRate)
                .licenseId(licenseId)
                .createAt(LocalDate.now())
                .updateAt(LocalDate.now())
                .build();
        serviceRepository.save(s);
        return "redirect:/admin/customers/{id}";
    }

    @PostMapping("/{id}/services/{sid}/update")
    public String updateService(@PathVariable String id,
                                @PathVariable Integer sid,
                                @RequestParam String serviceName,
                                @RequestParam(required = false) String domain,
                                @RequestParam(required = false) String productType,
                                @RequestParam(required = false) Integer cpiValue,
                                @RequestParam(required = false) Double rsRate,
                                @RequestParam(required = false) Integer licenseId) {
        Service s = serviceRepository.findByServiceIdAndCustomerId(sid, id).orElseThrow();
        s.setServiceName(serviceName);
        s.setDomain(blankToNull(domain));
        s.setProductType(blankToNull(productType));
        s.setCpiValue(cpiValue);
        s.setRsRate(rsRate);
        s.setLicenseId(licenseId);
        s.setUpdateAt(LocalDate.now());
        serviceRepository.save(s);
        // 수정 후 서비스 상세에 머무르기
        return "redirect:/admin/customers/{id}/services/{sid}";
    }

    @PostMapping("/{id}/services/{sid}/delete")
    public String deleteService(@PathVariable String id, @PathVariable Integer sid) {
        serviceContactRepository.deleteByCustomerIdAndServiceId(id, sid);
        serviceRepository.deleteById(sid);
        return "redirect:/admin/customers/{id}";
    }

    /* ★ 서비스 상세 (연락처/로그/라이선스 섹션 포함) */
    @GetMapping("/{id}/services/{sid}")
    public String serviceDetail(@PathVariable String id,
                                @PathVariable Integer sid,
                                @RequestParam(required = false) String from,
                                @RequestParam(required = false) String to,
                                @RequestParam(required = false) String q,
                                @RequestParam(required = false) String level,
                                Model model) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객사를 찾을 수 없습니다: " + id));
        Service service = serviceRepository.findByServiceIdAndCustomerId(sid, id)
                .orElseThrow(() -> new IllegalArgumentException("서비스를 찾을 수 없습니다: " + sid));

        var contacts = serviceContactRepository.findByCustomerIdAndServiceIdOrderByCreateAtDesc(id, sid);
        var logs = Collections.emptyList(); // 이후 실제 조회로 교체

        model.addAttribute("customer", customer);
        model.addAttribute("service", service);
        model.addAttribute("contacts", contacts);
        model.addAttribute("logs", logs);

        Map<String, Object> params = new HashMap<>();
        params.put("from", from); params.put("to", to);
        params.put("q", q); params.put("level", level);
        model.addAttribute("params", params);

        return "admin/customer/service_detail";
    }

    /* 라이선스 발급/재발급 */
    @PostMapping("/{id}/services/{sid}/license")
    public String issueLicense(@PathVariable String id, @PathVariable Integer sid) {
        Service s = serviceRepository.findByServiceIdAndCustomerId(sid, id).orElseThrow();
        int newLicense = 100000 + new Random().nextInt(900000); // 예시
        s.setLicenseId(newLicense);
        s.setUpdateAt(LocalDate.now());
        serviceRepository.save(s);
        return "redirect:/admin/customers/{id}/services/{sid}";
    }

    private static String blankToNull(String s){
        return (s == null || s.isBlank()) ? null : s;
    }

    // ⚠️ 중복 매핑 메서드(/admin/customers/admin/customers)는 제거했습니다.
}
