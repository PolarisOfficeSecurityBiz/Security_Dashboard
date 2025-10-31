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
    ) {
        // 1) 파라미터 검증
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body(new byte[0]);
        }
        if (service == null || service.isBlank()) {
            service = "전체";
        }

        try {
            YearMonth ym = YearMonth.of(LocalDate.now().getYear(), month);
            int daysInMonth = ym.lengthOfMonth();

            // 데모 데이터 (실사용 시 DB/Service에서 가져오세요)
            List<DailyPartnerData> list = new ArrayList<>();
            for (int d = 1; d <= daysInMonth; d++) {
                list.add(new DailyPartnerData(d, 120 + d, 80 + d / 2, 30 + d / 3,
                        "제휴사 B".equals(service) ? 1200 : 1000,
                        "제휴사 B".equals(service) ? 150 : 200));
            }

            // 2) 워크북 생성 (try-with-resources로 안전 종료)
            byte[] bytes;
            try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                // 시트명은 31자 제한 + 금지문자 제거
                String safeSheetName = sanitizeSheetName(service + " 정산내역");
                Sheet sheet = wb.createSheet(safeSheetName);

                // 간단한 스타일(굵은 헤더 + 천단위 포맷)
                CellStyle headerStyle = wb.createCellStyle();
                Font bold = wb.createFont(); bold.setBold(true);
                headerStyle.setFont(bold);

                DataFormat df = wb.createDataFormat();
                CellStyle moneyStyle = wb.createCellStyle();
                moneyStyle.setDataFormat(df.getFormat("#,##0"));

                // Header
                String[] headers = {"일자", "유입 수", "이탈 수", "유지 수", "CPI(원)", "RS(원)", "정산 금액"};
                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell c = header.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                // Data
                int rowIdx = 1;
                for (DailyPartnerData d : list) {
                    Row r = sheet.createRow(rowIdx++);
                    r.createCell(0).setCellValue(d.getDay() + "일");
                    r.createCell(1).setCellValue(d.getJoin());
                    r.createCell(2).setCellValue(d.getLeave());
                    r.createCell(3).setCellValue(d.getRetain());

                    Cell cpi = r.createCell(4);
                    cpi.setCellValue(d.getCpi());
                    cpi.setCellStyle(moneyStyle);

                    Cell rs = r.createCell(5);
                    rs.setCellValue(d.getRsRate());
                    rs.setCellStyle(moneyStyle);

                    long total = (long) d.getJoin() * d.getCpi() + (long) d.getRetain() * d.getRsRate();
                    Cell totalCell = r.createCell(6);
                    totalCell.setCellValue(total);
                    totalCell.setCellStyle(moneyStyle);
                }

                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

                wb.write(out);
                bytes = out.toByteArray();
            }

            // 3) 응답 헤더 (한글 파일명 안전 인코딩)
            String filename = month + "월_" + service.replace(' ', '_') + "_정산내역.xlsx";
            String encoded = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(
                    org.springframework.http.MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            // filename* 사용으로 한글/공백 안전
            headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encoded);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            return ResponseEntity.ok().headers(headers).body(bytes);

        } catch (Exception e) {
            // 서버 로그에만 스택 출력하시고, 사용자에겐 500 코드만
            return ResponseEntity.status(500).body(new byte[0]);
        }
    }

    /** 엑셀 시트명 안전화 (금지문자 제거 + 31자 제한) */
    private String sanitizeSheetName(String name) {
        String n = name.replaceAll("[:\\\\/?*\\[\\]]", "_");
        if (n.length() > 31) n = n.substring(0, 31);
        // 시트명이 공백만 남는 경우 대비
        if (n.isBlank()) n = "Sheet1";
        return n;
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
