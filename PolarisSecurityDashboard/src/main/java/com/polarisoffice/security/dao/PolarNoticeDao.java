package com.polarisoffice.security.dao;

import com.google.cloud.firestore.*;
import com.polarisoffice.security.controller.AdminCustomerController;
import com.polarisoffice.security.model.PolarLetter;
import com.polarisoffice.security.model.PolarNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PolarNoticeDao {

    private final Firestore firestore;
    private static final String COL = "polarNotice";  // Firestore collection 이름

    // PolarNotice 객체로 변환하는 메서드 (DocumentSnapshot -> PolarNotice)
    private PolarNotice map(DocumentSnapshot document) {
        PolarNotice notice = document.toObject(PolarNotice.class);  // Firestore 문서를 PolarNotice 객체로 변환
        if (notice != null) {
            notice.setId(document.getId());  // Firestore에서 자동으로 생성된 ID 설정
        }
        return notice;
    }

    // 모든 공지사항 가져오는 메서드
    public List<PolarNotice> findAll() throws Exception {
        // Firestore에서 모든 문서를 가져오는 쿼리
        Query query = firestore.collection(COL)
            .orderBy("date", Query.Direction.DESCENDING); // 날짜순 정렬

        // Firestore에서 문서 가져오기
        List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();

        // 문서에서 PolarNotice 객체로 변환
        List<PolarNotice> rows = docs.stream()
            .map(this::map)  // map() 메서드를 통해 PolarNotice 객체로 변환
            .collect(Collectors.toList());

        return rows;  // 모든 공지사항 반환
    }
}
