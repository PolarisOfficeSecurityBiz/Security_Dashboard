package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.SecuNewsDao;
import com.polarisoffice.security.dto.SecuNewsCreateRequest;
import com.polarisoffice.security.dto.SecuNewsUpdateRequest;
import com.polarisoffice.security.model.SecuNews;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
