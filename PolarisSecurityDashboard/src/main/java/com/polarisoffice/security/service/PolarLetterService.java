package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.PolarLetterDao;
import com.polarisoffice.security.model.PolarLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * PolarLetter 서비스
 * - 목록/단건 조회
 * - 생성/부분수정/삭제
 * - 최근 7일 delta, 전체 count
 */
@Service
@RequiredArgsConstructor
public class PolarLetterService {

    private final PolarLetterDao dao;

    /** 목록 조회 */
    public List<PolarLetter> getAll(Integer size, String q) {
        int fetch = (size == null || size <= 0) ? 200 : Math.min(size, 1000);
        return dao.findAll(fetch, q);
    }

    /** 단건 조회 */
    public Optional<PolarLetter> getById(String id) {
        return dao.findById(id);
    }

    /** 생성 */
    public PolarLetter create(PolarLetter req) {
        PolarLetter toSave = new PolarLetter();
        // 필드 매핑
        toSave.setId(null); // 생성 시 ID는 DAO/DB가 부여하거나, req.getId()를 허용하면 변경
        toSave.setTitle(nz(req.getTitle()));
        toSave.setAuthor(nz(req.getAuthor()));
        toSave.setCreateTime(normalizeDate(req.getCreateTime())); // "2025-09-12" 등으로 정규화
        toSave.setUrl(nz(req.getUrl()));
        toSave.setThumbnail(nz(req.getThumbnail()));
        toSave.setContent(nz(req.getContent()));

        // dao.create(...)가 없다면 dao.save(toSave)로 대체
        // return dao.save(toSave);
        return dao.create(toSave);
    }

    /** 부분 수정 (null이 아닌 필드만 반영, 빈 문자열은 빈 값으로 반영) */
    public PolarLetter update(String id, PolarLetter patch) {
        PolarLetter cur = dao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PolarLetter not found: " + id));

        // null 이면 유지, 값이 있으면 설정(빈 문자열도 의도적 값으로 인정)
        if (patch.getTitle() != null)      cur.setTitle(patch.getTitle());
        if (patch.getAuthor() != null)     cur.setAuthor(patch.getAuthor());
        if (patch.getCreateTime() != null) cur.setCreateTime(normalizeDate(patch.getCreateTime()));
        if (patch.getUrl() != null)        cur.setUrl(patch.getUrl());
        if (patch.getThumbnail() != null)  cur.setThumbnail(patch.getThumbnail());
        if (patch.getContent() != null)    cur.setContent(patch.getContent());

        // dao.update(...)가 없다면 dao.save(cur)로 대체
        // return dao.save(cur);
        return dao.update(id, cur);
    }

    /** 삭제 */
    public void delete(String id) {
        // 존재 확인 후 삭제(선택)
        dao.findById(id).orElseThrow(() -> new NoSuchElementException("PolarLetter not found: " + id));
        dao.delete(id);
    }

    /** 최근 7일 이내 생성된 수 */
    public int getDelta() {
        try {
            List<PolarLetter> letters = dao.findAll(200, null);
            long nowMs = System.currentTimeMillis();
            long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;

            int delta = 0;
            for (PolarLetter l : letters) {
                Date created = parseDateLenient(l.getCreateTime());
                if (created == null) continue;
                if (nowMs - created.getTime() <= sevenDaysMs) {
                    delta++;
                }
            }
            return delta;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch letter delta", e);
        }
    }

    /** 전체 개수 */
    public int getCount() {
        try {
            // 가능하면 DAO에 count() 구현
            // return dao.count();
            try {
                return dao.count();
            } catch (UnsupportedOperationException | NoSuchMethodError ignored) {
                List<PolarLetter> letters = dao.findAll(1000, null);
                return letters.size();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch letter count", e);
        }
    }

    /* =========================
              Utils
       ========================= */

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /**
     * UI에서 들어오는 다양한 날짜 포맷을 "yyyy-MM-dd"로 정규화.
     * 지원:
     *  - yyyy-MM-dd (그대로 통과)
     *  - yyyy.MM.dd  (점 → 대시)
     *  - ISO-8601 (e.g. 2025-09-12T08:35:22Z)
     *  - 그 외 파싱 실패 시 원문 유지
     */
    private static String normalizeDate(String in) {
        if (in == null || in.isBlank()) return in;

        // 이미 yyyy-MM-dd 형태면 그대로
        if (in.matches("\\d{4}-\\d{2}-\\d{2}")) return in;

        // yyyy.MM.dd → yyyy-MM-dd
        if (in.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return in.replace('.', '-');
        }

        // ISO-8601 시도
        try {
            // OffsetDateTime(예: Z 포함), LocalDateTime, LocalDate 모두 대응
            try {
                OffsetDateTime odt = OffsetDateTime.parse(in);
                return odt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) { }
            try {
                LocalDateTime ldt = LocalDateTime.parse(in);
                return ldt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) { }
            try {
                LocalDate ld = LocalDate.parse(in);
                return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) { }
        } catch (Exception ignored) { }

        // 마지막으로 구 SimpleDateFormat 시도
        Date d = parseDateLenient(in);
        if (d != null) {
            return new SimpleDateFormat("yyyy-MM-dd").format(d);
        }

        // 실패 시 원문 반환(서버/DB 제약에 맞춰 필요하면 null로 바꿔도 됨)
        return in;
    }

    /** 다양한 포맷으로 Date 파싱 (실패 시 null) */
    private static Date parseDateLenient(String s) {
        if (s == null || s.isBlank()) return null;

        // 우선 ISO-8601
        try {
            // Z/오프셋 포함
            Instant inst = Instant.parse(s);
            return Date.from(inst);
        } catch (Exception ignored) { }
        try {
            // 오프셋 없는 로컬 날짜시간
            LocalDateTime ldt = LocalDateTime.parse(s);
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) { }
        try {
            // 로컬 날짜
            LocalDate ld = LocalDate.parse(s);
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
