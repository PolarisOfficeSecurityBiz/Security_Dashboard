package com.polarisoffice.security.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 일별 정산 내역 Controller
 *
 * - GET /customer/settlement : 페이지 렌더링
 * - GET /customer/settlement/api?month=10 : 선택 월별 데이터 JSON 응답
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/settlement")
public class SettlementController {

    /**
     * ✅ 정산 내역 페이지 렌더링
     */
    @GetMapping
    public String settlement(Model model,
                             @RequestParam(required = false) Integer month) {

        LocalDate today = LocalDate.now();
        int targetMonth = (month != null) ? month : today.getMonthValue();
        YearMonth ym = YearMonth.of(today.getYear(), targetMonth);

        // 1️⃣ 유입/이탈/유지 현황 (더미)
        DailyStatus daily = new DailyStatus(
                ym.getMonthValue(),
                today.getDayOfMonth(),
                125, 30, 95
        );

        // 2️⃣ 제휴사 정산 내역 (더미)
        List<PartnerSettlement> partners = new ArrayList<>();
        partners.add(new PartnerSettlement("제휴사 A", 210, 1000, 180, 200, ym.lengthOfMonth()));
        partners.add(new PartnerSettlement("제휴사 B", 100, 1200, 70, 150, ym.lengthOfMonth()));

        // 3️⃣ 모델에 데이터 주입
        model.addAttribute("today", today);
        model.addAttribute("daily", daily);
        model.addAttribute("partners", partners);
        model.addAttribute("month", ym);

        return "customer/settlement";
    }

    /**
     * ✅ Ajax 요청 처리 (월별 데이터 반환)
     * URL 예: /customer/settlement/api?month=9
     */
    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> getSettlementData(@RequestParam int month) {
        YearMonth ym = YearMonth.of(LocalDate.now().getYear(), month);

        // 더미 데이터 (DB 연동 시 여기서 서비스 호출)
        List<PartnerSettlement> partners = new ArrayList<>();
        partners.add(new PartnerSettlement("제휴사 A", 200 + month, 1000, 180, 200, ym.lengthOfMonth()));
        partners.add(new PartnerSettlement("제휴사 B", 120 + month, 1200, 70, 150, ym.lengthOfMonth()));

        Map<String, Object> response = new HashMap<>();
        response.put("month", month);
        response.put("days", ym.lengthOfMonth());
        response.put("partners", partners);
        return response;
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
