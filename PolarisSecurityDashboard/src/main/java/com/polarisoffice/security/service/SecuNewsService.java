package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.SecuNewsDao;
import com.polarisoffice.security.dto.SecuNewsCreateRequest;
import com.polarisoffice.security.dto.SecuNewsUpdateRequest;
import com.polarisoffice.security.model.SecuNews;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecuNewsService {

    private final SecuNewsDao dao;

    // ---- 날짜 파싱 유틸 설정 ----
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    // 날짜만 있는 형태
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,                  // 2025-05-23
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),         // 2025.05.23
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),         // 2025/05/23
            DateTimeFormatter.ofPattern("yyyyMMdd")            // 20250523
    );
    // 날짜+시간 형태
    private static final List<DateTimeFormatter> DATETIME_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,             // 2025-05-23T13:45:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm[:ss]"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm[:ss]")
    );
    // (옵션) 타임존 포함된 문자열까지 들어온다면 추가
    private static final List<DateTimeFormatter> OFFSET_FORMATS = List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME             // 2025-05-23T10:00:00+09:00
    );

    private static String clean(String s) {
        if (s == null) return "";
        // NBSP 등 이상 공백 제거 후 trim
        return s.replace('\u00A0', ' ').trim();
    }

    /** 문자열을 LocalDateTime으로 유연하게 파싱 */
    private static LocalDateTime parseFlexibleDateTime(String raw) {
        String s = clean(raw);
        if (s.isEmpty()) throw new DateTimeParseException("empty date", s, 0);

        // 1) 날짜+시간
        for (DateTimeFormatter f : DATETIME_FORMATS) {
            try { return LocalDateTime.parse(s, f); } catch (DateTimeParseException ignore) {}
        }
        // 2) 타임존 포함
        for (DateTimeFormatter f : OFFSET_FORMATS) {
            try {
                var odt = OffsetDateTime.parse(s, f);
                return odt.atZoneSameInstant(ZONE).toLocalDateTime();
            } catch (DateTimeParseException ignore) {}
        }
        // 3) 날짜만 → 자정으로
        for (DateTimeFormatter f : DATE_FORMATS) {
            try { return LocalDate.parse(s, f).atStartOfDay(); } catch (DateTimeParseException ignore) {}
        }
        // 4) 구분자 통일 후 재시도
        String norm = s.replace('.', '-').replace('/', '-').replaceAll("\\s+", " ");
        if (!norm.equals(s)) return parseFlexibleDateTime(norm);

        throw new DateTimeParseException("Unsupported date format", s, 0);
    }

    // ----------------------------------------------------------------

    /** 목록 */
    public List<SecuNews> getAll(Integer size, String q, String category) {
        int fetchSize = (size == null || size <= 0) ? 200 : Math.min(size, 1000);
        return dao.findAll(fetchSize, q, category);
    }

    /** 단건 */
    public Optional<SecuNews> getById(String id) {
        return dao.findById(id);
    }

    /** 생성 */
    public SecuNews create(SecuNewsCreateRequest req) {
        SecuNews news = SecuNews.builder()
                .title(req.getTitle())
                .category(req.getCategory())
                .date(req.getDate())
                .thumbnail(req.getThumbnail())
                .url(req.getUrl())
                .build(); // updatedAt 은 DAO에서 세팅

        String id = dao.create(news);
        news.setId(id);
        return news;
    }

    /** 부분 수정 */
    public SecuNews update(String id, SecuNewsUpdateRequest req) {
        SecuNews patch = SecuNews.builder()
                .title(req.getTitle())
                .category(req.getCategory())
                .date(req.getDate())
                .thumbnail(req.getThumbnail())
                .url(req.getUrl())
                .build(); // updatedAt 은 DAO에서 세팅

        return dao.update(id, patch);
    }

    /** 삭제 */
    public void delete(String id) {
        dao.delete(id);
    }

    /** 최근 7일 이내의 보안 뉴스 개수 */
    public int getDelta() {
        try {
            List<SecuNews> newsList = dao.findAll(200, null, null);
            if (newsList == null) newsList = new ArrayList<>();

            LocalDate today = LocalDate.now(ZONE);
            int delta = 0;

            for (SecuNews news : newsList) {
                String dateString = news.getDate(); // 다양한 포맷이 섞여 있을 수 있음
                try {
                    LocalDateTime dt = parseFlexibleDateTime(dateString);
                    long days = ChronoUnit.DAYS.between(dt.toLocalDate(), today);
                    if (days >= 0 && days <= 7) {
                        delta++;
                    }
                } catch (DateTimeParseException ex) {
                    // 포맷이 완전히 깨진 항목은 스킵 (원하면 로그만 남기세요)
                    // log.warn("Invalid date: {}", dateString, ex);
                }
            }
            return delta;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news delta", e);
        }
    }

    /** 전체 SecuNews 개수 */
    public int getCount() {
        try {
            List<SecuNews> newsList = dao.findAll(200, null, null);
            return newsList == null ? 0 : newsList.size();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news count", e);
        }
    }

    // (옵션) 저장 시 포맷을 일괄 정규화하고 싶다면 사용
    /** 입력 문자열을 표준 포맷(yyyy.MM.dd)으로 변환 */
    public static String normalizeDate(String raw) {
        try {
            LocalDateTime dt = parseFlexibleDateTime(raw);
            return dt.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        } catch (Exception e) {
            return clean(raw); // 실패 시 원문 유지
        }
    }
}
