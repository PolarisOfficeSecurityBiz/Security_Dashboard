package com.polarisoffice.security.model.edit;
public enum EditRequestStatus {
    PENDING,   // 대기
    IN_PROGRESS, // 처리중
    RESOLVED,  // 처리완료
    REJECTED   // 반려
}