package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.PolarLetterDao;
import com.polarisoffice.security.model.PolarLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolarLetterService {

    private final PolarLetterDao dao;

    // 모든 PolarLetter 항목을 검색 (검색어(q)와 최대 개수(size) 포함)
    public List<PolarLetter> getAll(Integer size, String q) {
        int fetch = (size == null || size <= 0) ? 200 : Math.min(size, 1000);
        return dao.findAll(fetch, q);
    }

    // 특정 ID로 PolarLetter 조회
    public Optional<PolarLetter> getById(String id) {
        return dao.findById(id);
    }

    // 최근 7일 이내의 새로운 PolarLetter 개수
    public int getDelta() {
        try {
            List<PolarLetter> letters = dao.findAll(200, null);  // 가장 최근 200개를 가져옴
            long currentTime = System.currentTimeMillis();  // 현재 시간 (밀리초 단위)
            int delta = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");  // 날짜 포맷 정의

            // 최근 7일 이내 생성된 PolarLetter 수 계산
            for (PolarLetter letter : letters) {
                String dateString = letter.getCreateTime();  // "2024.12.23" 형식의 날짜 문자열
                try {
                    Date createDate = sdf.parse(dateString);  // 문자열을 Date로 변환
                    long createTime = createDate.getTime();  // Date 객체에서 밀리초로 시간 추출
                    if (currentTime - createTime <= 7L * 24 * 60 * 60 * 1000) {  // 7일 이내
                        delta++;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();  // 파싱 예외 처리
                }
            }
            return delta;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch letter delta", e);
        }
    }

    // 전체 PolarLetter 개수 반환
    public int getCount() {
        try {
            List<PolarLetter> letters = dao.findAll(200, null);  // 모든 PolarLetter 항목 가져오기 (필요에 따라 size 조정 가능)
            return letters.size();  // 전체 개수 반환
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch letter count", e);
        }
    }

    // create/update/delete 필요 시 dao 위임 메서드 추가
}
