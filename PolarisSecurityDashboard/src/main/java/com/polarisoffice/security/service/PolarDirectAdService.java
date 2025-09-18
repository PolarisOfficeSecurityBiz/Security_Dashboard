package com.polarisoffice.security.service;

import com.google.cloud.Timestamp;
import com.polarisoffice.security.model.PolarDirectAd;
import com.polarisoffice.security.dto.PolarDirectAdCreateRequest;
import com.polarisoffice.security.dto.PolarDirectAdUpdateRequest;
import com.polarisoffice.security.dao.PolarDirectAdDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class PolarDirectAdService {

    private final PolarDirectAdDao dao;

    /** 전체 조회 (동기) */
    public List<PolarDirectAd> getAll() {
    	return dao.findAll();
    }

    /** 단건 조회 (동기) */
    public Optional<PolarDirectAd> getById(String id) {
        return Optional.ofNullable(dao.findById(id));
    }

    /** 생성 (동기) */
    public PolarDirectAd create(PolarDirectAdCreateRequest req) {
        PolarDirectAd ad = PolarDirectAd.builder()
                .adType(req.getAdType())                 // Enum/String 타입은 모델에 맞춰 사용
                .advertiserName(req.getAdvertiserName())
                .backgroundColor(req.getBackgroundColor())
                .imageUrl(req.getImageUrl())
                .targetUrl(req.getTargetUrl())
                .clickCount(0L)
                .viewCount(0L)
                .publishedDate(Timestamp.now())
                .updateAt(Timestamp.now())
                .build();

        String id = dao.create(ad);  // 저장 후 생성된 문서 ID 반환
        ad.setId(id);
        return ad;
    }

    /** 부분 수정 (동기) */
    public PolarDirectAd update(String id, PolarDirectAdUpdateRequest req) {
        // patch 객체 생성
        PolarDirectAd patch = PolarDirectAd.builder()
                .adType(req.getAdType())
                .advertiserName(req.getAdvertiserName())
                .backgroundColor(req.getBackgroundColor())
                .imageUrl(req.getImageUrl())
                .targetUrl(req.getTargetUrl())
                .clickCount(req.getClickCount())
                .viewCount(req.getViewCount())
                .build();

        return dao.update(id, patch);
    }

    /** 삭제 (동기) */
    public void delete(String id) {
        dao.delete(id);
    }
}
