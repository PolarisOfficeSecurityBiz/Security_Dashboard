package com.polarisoffice.security.dto;

import java.time.LocalDate;        // ✅ 꼭 필요
import java.time.LocalDateTime;    // ✅ 둘 다 지원
import java.time.format.DateTimeFormatter;

public record RecentCustomerRow(
        String id,
        String customerName,
        String connectedCompanyName,
        String createAt // 화면에는 문자열로 표기
) {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ✅ LocalDateTime → String
    public RecentCustomerRow(String id,
                             String customerName,
                             String connectedCompanyName,
                             LocalDateTime createdAt) {
        this(id, customerName, connectedCompanyName,
                createdAt != null ? createdAt.format(FMT) : null);
    }

    // ✅ LocalDate → String
    public RecentCustomerRow(String id,
                             String customerName,
                             String connectedCompanyName,
                             LocalDate createdAt) {
        this(id, customerName, connectedCompanyName,
                createdAt != null ? createdAt.toString() : null);
    }
}
