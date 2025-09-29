package com.polarisoffice.security.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.polarisoffice.security.model.SecuNews;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class SecuNewsDao {

    private final Firestore firestore;
    private static final String COL = "secuNews";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** ApiFuture 공통 처리 */
    private static <T> T await(ApiFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling Firestore", ie);
        } catch (ExecutionException ee) {
            Throwable cause = (ee.getCause() != null) ? ee.getCause() : ee;
            throw new IllegalStateException("Firestore call failed", cause);
        }
    }

    /** 안전 수동 매핑: updateAt이 String/Timestamp 섞여 있어도 처리 */
    private static SecuNews map(DocumentSnapshot d) {
        if (d == null || !d.exists()) return null;

        String id        = d.getId();
        String category  = d.getString("category");
        String title     = d.getString("title");
        String url       = d.getString("url");
        String thumbnail = d.getString("thumbnail");
        String date      = d.getString("date");

        // updateAt은 과거 문서에서 String일 수 있음 → 유연 처리
        Timestamp ts = null;
        Object u = d.get("updateAt");
        if (u instanceof Timestamp) {
            ts = (Timestamp) u;
        } else if (u instanceof String s && !s.isBlank()) {
            try {
                // ISO-8601 문자열이라면 파싱해서 Timestamp로 변환
                Instant inst = Instant.parse(s.trim());
                ts = Timestamp.ofTimeSecondsAndNanos(inst.getEpochSecond(), inst.getNano());
            } catch (Exception ignore) {
                // 이상한 포맷이면 그냥 무시
                System.out.println("[SecuNewsDao] updateAt is string (unparsable), ignored: " + s);
            }
        }

        return SecuNews.builder()
                .id(id)
                .category(category)
                .title(title)
                .url(url)
                .thumbnail(thumbnail)
                .date(date)
                .updateAt(ts)
                .build();
    }

    /** 목록 */
    public List<SecuNews> findAll(int size, String q, String category) {
        CollectionReference col = firestore.collection(COL);
        Query query = col;

        if (category != null && !category.isBlank()) {
            query = query.whereEqualTo("category", category);
        }
        // 최신순(문자열 날짜 기준)
        query = query.orderBy("date", Query.Direction.DESCENDING);
        if (size > 0) query = query.limit(size);

        QuerySnapshot snap = await(query.get());

        String kw = (q == null) ? "" : q.trim().toLowerCase();
        List<SecuNews> list = new ArrayList<>();

        for (DocumentSnapshot d : snap.getDocuments()) {
            SecuNews n = map(d);
            if (n == null) continue;

            if (kw.isEmpty()
                    || (n.getTitle() != null && n.getTitle().toLowerCase().contains(kw))
                    || (n.getCategory() != null && n.getCategory().toLowerCase().contains(kw))
                    || (n.getUrl() != null && n.getUrl().toLowerCase().contains(kw))) {
                list.add(n);
            }
        }
        return list;
    }

    /** 단건 */
    public Optional<SecuNews> findById(String id) {
        DocumentSnapshot snap = await(firestore.collection(COL).document(id).get());
        if (!snap.exists()) return Optional.empty();
        return Optional.ofNullable(map(snap));
    }

    /** 생성 */
    public String create(SecuNews news) {
        if (news.getCategory() == null || news.getCategory().isBlank()) {
            news.setCategory("보안뉴스");
        }
        if (news.getDate() == null || news.getDate().isBlank()) {
            news.setDate(LocalDate.now(KST).format(DATE));
        }

        // @ServerTimestamp 필드는 null로 두고 저장 → 서버가 채움
        news.setUpdateAt(null);

        DocumentReference ref = firestore.collection(COL).document();
        await(ref.set(news, SetOptions.merge()));

        // 생성 시점에 서버타임스탬프 확실히 찍고 싶으면 한 번 더 merge
        Map<String, Object> serverTs = Map.of("updateAt", FieldValue.serverTimestamp());
        await(ref.set(serverTs, SetOptions.merge()));

        return ref.getId();
    }

    /** 부분수정 */
    public SecuNews update(String id, SecuNews patch) {
        DocumentReference ref = firestore.collection(COL).document(id);
        Map<String, Object> upd = new HashMap<>();

        if (patch.getTitle() != null)     upd.put("title", patch.getTitle());
        if (patch.getUrl() != null)       upd.put("url", patch.getUrl());
        if (patch.getCategory() != null)  upd.put("category", patch.getCategory());
        if (patch.getThumbnail() != null) upd.put("thumbnail", patch.getThumbnail());
        if (patch.getDate() != null)      upd.put("date", patch.getDate());

        // 서버 타임스탬프로 updateAt 갱신
        upd.put("updateAt", FieldValue.serverTimestamp());

        await(ref.set(upd, SetOptions.merge()));

        // 최신 상태 반환
        DocumentSnapshot after = await(ref.get());
        return map(after);
    }

    /** 삭제 */
    public void delete(String id) {
        await(firestore.collection(COL).document(id).delete());
    }
}
