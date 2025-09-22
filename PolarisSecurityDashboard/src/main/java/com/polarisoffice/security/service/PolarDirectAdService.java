package com.polarisoffice.security.service;

import com.google.cloud.Timestamp;
import com.polarisoffice.security.model.PolarDirectAd;
import com.polarisoffice.security.dto.PolarDirectAdCreateRequest;
import com.polarisoffice.security.dto.PolarDirectAdUpdateRequest;
import com.polarisoffice.security.dao.PolarDirectAdDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    /** 생성 */
    public PolarDirectAd create(PolarDirectAdCreateRequest req) {
        PolarDirectAd ad = PolarDirectAd.builder()
                .adType(req.getAdType())   // ← 변환 없이 그대로
                .advertiserName(req.getAdvertiserName())
                .backgroundColor(req.getBackgroundColor())
                .imageUrl(req.getImageUrl())
                .targetUrl(req.getTargetUrl())
                .clickCount(0L)
                .viewCount(0L)
                .publishedDate(Timestamp.now())
                .updatedAt(Timestamp.now())
                .build();

        String id = dao.create(ad);
        ad.setId(id);
        return ad;
    }

    /** 부분 수정 */
    public PolarDirectAd update(String id, PolarDirectAdUpdateRequest req) {
        PolarDirectAd patch = new PolarDirectAd();
        if (req.getAdType() != null)         patch.setAdType(req.getAdType()); // ← 그대로
        if (req.getAdvertiserName() != null) patch.setAdvertiserName(req.getAdvertiserName());
        if (req.getBackgroundColor() != null)patch.setBackgroundColor(req.getBackgroundColor());
        if (req.getImageUrl() != null)       patch.setImageUrl(req.getImageUrl());
        if (req.getTargetUrl() != null)      patch.setTargetUrl(req.getTargetUrl());
        if (req.getClickCount() != null)     patch.setClickCount(req.getClickCount());
        if (req.getViewCount() != null)      patch.setViewCount(req.getViewCount());
        return dao.update(id, patch);
    }

    /** 삭제 (동기) */
    public void delete(String id) {
        dao.delete(id);
    }

    /** 전체 PolarDirectAd 개수 */
    public int getCount() {
        try {
            List<PolarDirectAd> ads = dao.findAll();  // 모든 광고 가져오기
            return ads.size();  // 전체 개수 반환
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch direct ad count", e);
        }
    }

    /** 최근 7일 이내의 광고 개수 */
 // PolarDirectAdService.java
    public int getDelta() {
        try {
            List<PolarDirectAd> ads = dao.findAll();  // 모든 광고 가져오기
            long currentTime = System.currentTimeMillis();  // 현재 시간 (밀리초 단위)
            int delta = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // 날짜 포맷 정의

            // 최근 7일 이내 생성된 광고 수 계산
            for (PolarDirectAd ad : ads) {
                String dateString = ad.getPublishedDate().toString();  // 날짜를 문자열로 가져오기
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
            e.printStackTrace();  // 예외 발생 시 스택 트레이스 출력
            throw new RuntimeException("Failed to fetch direct ad delta", e);  // 예외를 던져서 호출자에게 알리기
        }
    }


}
