package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.PolarNoticeDao;
import com.polarisoffice.security.model.PolarNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolarNoticeService {

    private final PolarNoticeDao dao;

    public List<PolarNotice> getAll() {
        try {
            return dao.findAll();  // DAO에서 데이터를 가져옵니다.
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch notices", e);
        }
    }

    // 공지사항의 개수 가져오기
    public int getCount() {
        try {
            return dao.findAll().size();  // 공지사항의 총 개수
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch notice count", e);
        }
    }

    // 신규 공지사항의 개수 (7일 이내 생성된 공지사항을 신규로 간주)
    public int getDelta() {
        try {
            List<PolarNotice> notices = dao.findAll();
            long currentTime = System.currentTimeMillis();  // 현재 시간 (밀리초 단위)
            int delta = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");  // 날짜 문자열 포맷

            // 최근 7일 이내 생성된 공지사항 수 계산
            for (PolarNotice notice : notices) {
                String dateString = notice.getDate();  // "2024.12.23" 형식의 날짜 문자열
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
            throw new RuntimeException("Failed to fetch notice delta", e);
        }
    }
}