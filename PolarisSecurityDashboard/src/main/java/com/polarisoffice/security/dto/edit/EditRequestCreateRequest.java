// src/main/java/com/polarisoffice/security/dto/EditRequestCreateRequest.java
package com.polarisoffice.security.dto.edit;

import lombok.Getter;
import lombok.Setter;

/**
 * company.js 가 보내는 payload
 * {
 *   "customerId": "xxx",
 *   "targetType": "COMPANY" | "SERVICE",
 *   "targetId": 123,              // SERVICE일 때만
 *   "message": "내용"
 * }
 */
@Getter @Setter
public class EditRequestCreateRequest {
    private String customerId;
    private String targetType; // COMPANY or SERVICE (대문자)
    private Integer targetId;  // SERVICE 면 serviceId로 사용
    private String message;
}
