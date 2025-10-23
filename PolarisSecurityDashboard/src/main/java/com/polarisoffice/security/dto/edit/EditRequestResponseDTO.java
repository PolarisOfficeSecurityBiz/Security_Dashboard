// src/main/java/com/polarisoffice/security/dto/edit/EditRequestResponseDTO.java
package com.polarisoffice.security.dto.edit;

import com.polarisoffice.security.model.edit.EditRequestStatus;
import com.polarisoffice.security.model.edit.EditTargetType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // ✅ 이거 있어야 service에서 .builder()가 됩니다
public class EditRequestResponseDTO {

    private Long id;

    private String customerId;
    private Integer serviceId;

    private EditTargetType targetType;

    private String content;

    private String requesterName;
    private String requesterEmail;

    private EditRequestStatus status;

    private LocalDateTime createAt; // 엔티티 필드명과 동일하게 createAt
    private LocalDateTime handledAt;

    private String handledBy;
    private String adminMemo;
}
