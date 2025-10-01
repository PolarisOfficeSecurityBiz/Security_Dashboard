package com.polarisoffice.security.dto;

import java.time.LocalDate;

public record CustomerRowDto(
        String customerId,
        String customerName,
        String connectedCompanyName,
        LocalDate createAt
) {}