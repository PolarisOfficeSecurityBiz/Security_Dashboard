package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Customer;
import com.polarisoffice.security.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/api/customers")
public class AdminCustomerController {

    private final CustomerRepository customerRepository;

    /**
     * ✅ Ajax용 JSON 응답
     * /admin/api/customers/list
     */
    @GetMapping("/list")
    @ResponseBody
    public List<Customer> getCustomerList() {
        return customerRepository.findAll();
    }

    /**
     * ✅ (기존) 페이지 뷰 호출
     * /admin/api/customers → admin/customers.html
     */
    @GetMapping
    public String listPage(Model model) {
        return "admin/customers";
    }
}
