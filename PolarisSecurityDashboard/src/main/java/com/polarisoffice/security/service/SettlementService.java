package com.polarisoffice.security.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.polarisoffice.security.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;

    public long getMonthlyTotal(String customerId, int month) {
        return settlementRepository.sumAmountByCustomerAndMonth(customerId, month);
    }

    public List<Map<String, Object>> getYearlySettlements(String customerId) {
        return settlementRepository.findByCustomerYearly(customerId);
    }
}