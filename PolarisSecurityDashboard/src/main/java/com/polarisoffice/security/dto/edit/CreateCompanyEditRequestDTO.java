// src/main/java/com/polarisoffice/security/dto/edit/CreateCompanyEditRequestDTO.java
package com.polarisoffice.security.dto.edit;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateCompanyEditRequestDTO {
    private String customerId; // 필수
    private String content;    // 필수
}
