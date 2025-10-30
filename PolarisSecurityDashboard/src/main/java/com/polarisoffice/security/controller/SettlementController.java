package com.polarisoffice.security.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 일별 정산 내역 Controller
 *
 * - GET /customer/settlement : 일별 유입/이탈/유지 현황 및 제휴사 정산 내역 표시
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/settlement")
public class SettlementController {

    @GetMapping
    public String settlement(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.from(today);

        // ✅ 1) 유입/이탈/유지 현황 (예시 데이터)
        DailyStatus daily = new DailyStatus(
                today.getMonthValue(),
                today.getDayOfMonth(),
                125,  // 유입
                30,   // 이탈
                95    // 유지
        );

        // ✅ 2) 제휴사 정산 내역 (예시)
        List<PartnerSettlement> partners = new ArrayList<>();
        partners.add(new PartnerSettlement("제휴사 A", 210, 1000, 180, 200, month.lengthOfMonth()));
        partners.add(new PartnerSettlement("제휴사 B", 100, 1200, 70, 150, month.lengthOfMonth()));

        // ✅ 3) 모델 주입
        model.addAttribute("today", today);
        model.addAttribute("daily", daily);
        model.addAttribute("partners", partners);
        model.addAttribute("month", month);

        return "customer/settlement"; // templates/customer/settlement.html
    }

    /* ================================
       내부 DTO 클래스
    =================================*/

    @Data
    @AllArgsConstructor
    public static class DailyStatus {
        private int month;
        private int day;
        private int join;   // 유입
        private int leave;  // 이탈
        private int retain; // 유지
    }

    @Data
    @AllArgsConstructor
    public static class PartnerSettlement {
        private String partnerName;
        private int joinCount;
        private int cpi;          // CPI 금액
        private int retainCount;
        private int rsRate;       // RS 금액
        private int days;         // 기간(일수)

        public long getTotalAmount() {
            return (long) (joinCount * cpi + retainCount * rsRate * days);
        }
    }
}
