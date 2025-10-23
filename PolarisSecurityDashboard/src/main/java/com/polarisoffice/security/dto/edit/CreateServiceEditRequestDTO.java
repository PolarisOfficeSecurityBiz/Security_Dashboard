// src/main/java/com/polarisoffice/security/dto/edit/CreateServiceEditRequestDTO.java
package com.polarisoffice.security.dto.edit;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateServiceEditRequestDTO {
    private String customerId; // 필수
    private Integer serviceId; // 필수
    private String content;    // 필수
}
