package com.polarisoffice.security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavModelAdvice {

    /** 모든 뷰에서 ${path}로 현재 요청 URI 사용 가능 */
    @ModelAttribute("path")
    public String exposeRequestPath(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : "";
    }
}