package com.polarisoffice.security.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 월별 / 일별 정산 내역 + 엑셀 다운로드
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/settlement")
public class SettlementController {

    @GetMapping
    public String settlement(Model model) {
        YearMonth month = YearMonth.now();

        model.addAttribute("month", month);
        model.addAttribute("services", List.of("전체", "제휴사 A", "제휴사 B"));
        return "customer/settlement";
    }

    /** ✅ Ajax 요청 : 월/서비스별 일별 데이터 */
    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> getSettlementData(
            @RequestParam int month,
            @RequestParam String service
    ) {
        YearMonth ym = YearMonth.of(LocalDate.now().getYear(), month);
        int days = ym.lengthOfMonth();

        List<DailyPartnerData> list = new ArrayList<>();

        Random random = new Random();
        for (int d = 1; d <= days; d++) {
            int join = 100 + random.nextInt(100);
            int retain = 50 + random.nextInt(50);
            int leave = 30 + random.nextInt(30);
            int cpi = service.equals("제휴사 B") ? 1200 : 1000;
            int rs = service.equals("제휴사 B") ? 150 : 200;

            list.add(new DailyPartnerData(d, join, retain, leave, cpi, rs));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("days", days);
        result.put("service", service);
        result.put("data", list);
        return result;
    }

    /** ✅ 엑셀 다운로드 */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam int month,
            @RequestParam String service
    ) throws Exception {
        YearMonth ym = YearMonth.of(LocalDate.now().getYear(), month);
        List<DailyPartnerData> list = new ArrayList<>();

        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            list.add(new DailyPartnerData(d, 120 + d, 80 + d / 2, 30 + d / 3, 1000, 200));
        }

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(service + " 정산내역");

        // Header
        Row header = sheet.createRow(0);
        String[] headers = {"일자", "유입 수", "이탈 수", "유지 수", "CPI(원)", "RS(원)", "정산 금액"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        // Data
        int rowIdx = 1;
        for (DailyPartnerData d : list) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(d.getDay() + "일");
            r.createCell(1).setCellValue(d.getJoin());
            r.createCell(2).setCellValue(d.getLeave());
            r.createCell(3).setCellValue(d.getRetain());
            r.createCell(4).setCellValue(d.getCpi());
            r.createCell(5).setCellValue(d.getRsRate());
            long total = d.getJoin() * d.getCpi() + d.getRetain() * d.getRsRate();
            r.createCell(6).setCellValue(total);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        String filename = month + "월_" + service + "_정산내역.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }

    /* 내부 DTO */
    @Data
    @AllArgsConstructor
    public static class DailyPartnerData {
        private int day;
        private int join;
        private int retain;
        private int leave;
        private int cpi;
        private int rsRate;
    }
}
