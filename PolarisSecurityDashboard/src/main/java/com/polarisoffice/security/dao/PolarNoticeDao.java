package com.polarisoffice.security.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.polarisoffice.security.model.PolarNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class PolarNoticeDao {

    private final Firestore firestore;
    private static final String COL = "polarNotice";  // Firestore collection name

    /* ===== ApiFuture 공통 대기 ===== */
    private static <T> T await(ApiFuture<T> f) {
        try {
            return f.get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling Firestore", ie);
        } catch (ExecutionException ee) {
            Throwable cause = (ee.getCause() != null) ? ee.getCause() : ee;
            throw new IllegalStateException("Firestore call failed", cause);
        }
    }

    private PolarNotice map(DocumentSnapshot d) {
        PolarNotice n = d.toObject(PolarNotice.class);
        if (n != null) n.setId(d.getId());
        return n;
    }

    /* ===== 목록 (date DESC) ===== */
    public List<PolarNotice> findAll(int size, String q) {
        Query query = firestore.collection(COL)
                .orderBy("date", Query.Direction.DESCENDING);
        if (size > 0) query = query.limit(size);

        List<QueryDocumentSnapshot> docs = await(query.get()).getDocuments();
        List<PolarNotice> rows = docs.stream().map(this::map).toList();

        // 서버측 문자열 필터
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            return rows.stream().filter(n ->
                    (n.getTitle()    != null && n.getTitle().toLowerCase().contains(needle)) ||
                    (n.getAuthor()   != null && n.getAuthor().toLowerCase().contains(needle)) ||
                    (n.getCategory() != null && n.getCategory().toLowerCase().contains(needle)) ||
                    (n.getContent()  != null && n.getContent().toLowerCase().contains(needle))
            ).toList();
        }
        return rows;
    }

    /* ===== 단건 ===== */
    public Optional<PolarNotice> findById(String id) {
        DocumentSnapshot snap = await(firestore.collection(COL).document(id).get());
        return snap.exists() ? Optional.of(map(snap)) : Optional.empty();
    }

    /* ===== 생성 ===== */
    public PolarNotice create(PolarNotice n) {
        // 등록일이 비어있으면 지금 시각 문자열(ISO)로 기록
        if (n.getDate() == null || n.getDate().isBlank()) {
            n.setDate(Instant.now().toString());
        }
        DocumentReference ref = firestore.collection(COL).document();
        n.setId(ref.getId());
        await(ref.set(n));
        return n;
    }

    /* ===== 부분 수정 (merge) ===== */
    public PolarNotice update(String id, PolarNotice patch) {
        DocumentReference ref = firestore.collection(COL).document(id);
        PolarNotice cur = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PolarNotice not found: " + id));

        // 필요한 필드만 반영 (null은 무시). 'date'는 등록일로 간주하여 덮어쓰지 않음.
        if (patch.getTitle()     != null) cur.setTitle(patch.getTitle());
        if (patch.getAuthor()    != null) cur.setAuthor(patch.getAuthor());
        if (patch.getCategory()  != null) cur.setCategory(patch.getCategory());
        if (patch.getImageURL()  != null) cur.setImageURL(patch.getImageURL());
        if (patch.getContent()   != null) cur.setContent(patch.getContent());
        // cur.setDate(...) 는 건드리지 않음

        await(ref.set(cur, SetOptions.merge()));
        return cur;
    }

    /* ===== 삭제 ===== */
    public void delete(String id) {
        await(firestore.collection(COL).document(id).delete());
    }

    /* ===== 전체 개수 ===== */
    public int count() {
        CollectionReference col = firestore.collection(COL);
        List<QueryDocumentSnapshot> docs = await(col.get()).getDocuments();
        return docs.size();
    }
}
