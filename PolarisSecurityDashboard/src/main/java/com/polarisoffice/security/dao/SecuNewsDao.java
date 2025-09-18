package com.polarisoffice.security.dao;

import com.google.api.core.ApiFuture;
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
    private static final DateTimeFormatter ISO  = DateTimeFormatter.ISO_INSTANT;

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

    private static SecuNews map(DocumentSnapshot d) {
        SecuNews n = d.toObject(SecuNews.class);
        if (n != null) n.setId(d.getId());
        return n;
    }

    /** 목록 */
    public List<SecuNews> findAll(int size, String q, String category) {

        CollectionReference col = firestore.collection(COL);
        Query query = col;

        if (category != null && !category.isBlank()) {
            query = query.whereEqualTo("category", category);
        }
        // 최신순
        query = query.orderBy("date", Query.Direction.DESCENDING);
        if (size > 0) query = query.limit(size);

        QuerySnapshot snap = await(query.get());

        String kw = (q == null) ? "" : q.trim().toLowerCase();
        List<SecuNews> list = new ArrayList<>();

        for (DocumentSnapshot d : snap.getDocuments()) {
            SecuNews n = map(d);
            if (n == null) continue;

            // 간단한 부분일치 필터 (서버에서 후처리)
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
        SecuNews n = map(snap);
        return Optional.ofNullable(n);
    }

    /** 생성 */
    public String create(SecuNews news) {
        if (news.getCategory() == null || news.getCategory().isBlank()) {
            news.setCategory("보안뉴스");
        }
        if (news.getDate() == null || news.getDate().isBlank()) {
            news.setDate(LocalDate.now(KST).format(DATE));
        }
        // ★ 필드명: updatedAt (오타 금지)
        news.setUpdateAt(ISO.format(Instant.now()));

        DocumentReference ref = firestore.collection(COL).document();
        await(ref.set(news));
        return ref.getId();
    }

    /** 부분수정 */
    public SecuNews update(String id, SecuNews patch) {
        DocumentReference ref = firestore.collection(COL).document(id);
        SecuNews cur = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("News not found: " + id));

        if (patch.getTitle() != null)     cur.setTitle(patch.getTitle());
        if (patch.getUrl() != null)       cur.setUrl(patch.getUrl());
        if (patch.getCategory() != null)  cur.setCategory(patch.getCategory());
        if (patch.getThumbnail() != null) cur.setThumbnail(patch.getThumbnail());
        if (patch.getDate() != null)      cur.setDate(patch.getDate());

        cur.setUpdateAt(ISO.format(Instant.now()));
        await(ref.set(cur, SetOptions.merge()));
        return cur;
    }

    /** 삭제 */
    public void delete(String id) {
        await(firestore.collection(COL).document(id).delete());
    }
}
