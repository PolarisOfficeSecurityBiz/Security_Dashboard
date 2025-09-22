package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.SecuNewsDao;
import com.polarisoffice.security.dto.SecuNewsCreateRequest;
import com.polarisoffice.security.dto.SecuNewsUpdateRequest;
import com.polarisoffice.security.model.SecuNews;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecuNewsService {

    private final SecuNewsDao dao;

    /** 목록 */
    public List<SecuNews> getAll(Integer size, String q, String category) {
        int fetchSize = (size == null || size <= 0) ? 200 : Math.min(size, 1000);
        return dao.findAll(fetchSize, q, category);
    }

    /** 단건 */
    public Optional<SecuNews> getById(String id) {
        return dao.findById(id); // Optional 그대로 반환
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
            List<SecuNews> newsList = dao.findAll(200, null, null); // 모든 뉴스 가져오기
            long currentTime = System.currentTimeMillis();  // 현재 시간 (밀리초 단위)
            int delta = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");  // 날짜 포맷 정의

            // 최근 7일 이내 생성된 SecuNews 수 계산
            for (SecuNews news : newsList) {
                String dateString = news.getDate();  // "2024.12.23" 형식의 날짜 문자열
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
            throw new RuntimeException("Failed to fetch news delta", e);
        }
    }

    /** 전체 SecuNews 개수 */
    public int getCount() {
        try {
            List<SecuNews> newsList = dao.findAll(200, null, null); // 모든 뉴스 항목 가져오기 (필요에 따라 size 조정 가능)
            return newsList.size();  // 전체 개수 반환
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news count", e);
        }
    }
}
