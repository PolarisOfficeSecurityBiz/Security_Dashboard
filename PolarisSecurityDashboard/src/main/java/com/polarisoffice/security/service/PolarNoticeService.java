package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.PolarNoticeDao;
import com.polarisoffice.security.model.PolarNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolarNoticeService {

    private final PolarNoticeDao dao;

    /** 목록 */
    public List<PolarNotice> getAll(Integer size, String q) {
        int fetch = (size == null || size <= 0) ? 200 : Math.min(size, 1000);
        return dao.findAll(fetch, q);
    }

    /** 단건 */
    public Optional<PolarNotice> getById(String id) {
        return dao.findById(id);
    }

    /** 생성 */
    public PolarNotice create(PolarNotice req) {
        return dao.create(req);
    }

    /** 부분 수정 */
    public PolarNotice update(String id, PolarNotice patch) {
        return dao.update(id, patch);
    }

    /** 삭제 */
    public void delete(String id) {
        dao.delete(id);
    }

    /** 전체 개수 (dao.count()가 있으면 사용, 없으면 findAll로 대체) */
    public int getCount() {
        try {
            try {
                return dao.count();
            } catch (UnsupportedOperationException | NoSuchMethodError e) {
                List<PolarNotice> rows = dao.findAll(1000, null);
                return rows.size();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch notice count", e);
        }
    }

    /** 최근 7일 이내 생성된 공지 수 (date 필드 기준) */
    public int getDelta() {
        try {
            List<PolarNotice> notices = dao.findAll(200, null);
            long nowMs = System.currentTimeMillis();
            long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;

            int delta = 0;
            for (PolarNotice n : notices) {
                Date created = parseDateLenient(n.getDate());
                if (created == null) continue;
                if (nowMs - created.getTime() <= sevenDaysMs) {
                    delta++;
                }
            }
            return delta;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch notice delta", e);
        }
    }

    /* =========================
                Utils
       ========================= */

    /**
     * 다양한 문자열 날짜를 Date로 파싱(실패 시 null)
     * 지원: ISO-8601(오프셋/UTC 포함), yyyy-MM-dd, yyyy.MM.dd, yyyy/MM/dd
     */
    private static Date parseDateLenient(String s) {
        if (s == null || s.isBlank()) return null;

        // ISO-8601 (예: 2025-09-12T08:35:22Z)
        try {
            Instant inst = Instant.parse(s);
            return Date.from(inst);
        } catch (Exception ignored) { }
        try {
            LocalDateTime ldt = LocalDateTime.parse(s);
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) { }
        try {
            LocalDate ld = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
            return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) { }

        // 구 포맷
        String[] patterns = { "yyyy-MM-dd", "yyyy.MM.dd", "yyyy/MM/dd" };
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p);
                sdf.setLenient(false);
                return sdf.parse(s);
            } catch (ParseException ignored) { }
        }
        return null;
    }
}
