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
    private static final String COL = "polarNotice";  // Firestore collection

    /* ===== 공통 유틸 ===== */
    private static <T> T await(ApiFuture<T> f) {
        try {
            return f.get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling Firestore", ie);
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
            throw new IllegalStateException("Firestore call failed", cause);
        }
    }

    private PolarNotice map(DocumentSnapshot d) {
        PolarNotice n = d.toObject(PolarNotice.class);
        if (n != null) n.setId(d.getId());
        return n;
    }

    /* ===== 목록 조회 (date DESC) ===== */
    public List<PolarNotice> findAll(int size, String q) {
        Query query = firestore.collection(COL)
                .orderBy("date", Query.Direction.DESCENDING);
        if (size > 0) query = query.limit(size);

        List<QueryDocumentSnapshot> docs = await(query.get()).getDocuments();
        List<PolarNotice> rows = docs.stream().map(this::map).toList();

        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            return rows.stream().filter(n ->
                    (n.getTitle() != null && n.getTitle().toLowerCase().contains(needle)) ||
                    (n.getAuthor() != null && n.getAuthor().toLowerCase().contains(needle)) ||
                    (n.getContent() != null && n.getContent().toLowerCase().contains(needle)) ||
                    (n.getCategory() != null && n.getCategory().toLowerCase().contains(needle))
            ).toList();
        }
        return rows;
    }

    /* ===== 단건 조회 ===== */
    public Optional<PolarNotice> findById(String id) {
        DocumentSnapshot snap = await(firestore.collection(COL).document(id).get());
        return snap.exists() ? Optional.of(map(snap)) : Optional.empty();
    }

    /* ===== 생성 ===== */
    public PolarNotice create(PolarNotice n) {
        if (n.getDate() == null) n.setDate(Instant.now().toString());
        DocumentReference ref = firestore.collection(COL).document();
        n.setId(ref.getId());
        await(ref.set(n));
        return n;
    }

    /* ===== 수정 (부분 병합) ===== */
    public PolarNotice update(String id, PolarNotice patch) {
        DocumentReference ref = firestore.collection(COL).document(id);
        PolarNotice cur = findById(id).orElseThrow(() -> new IllegalArgumentException("PolarNotice not found: " + id));

        if (patch.getTitle() != null)     cur.setTitle(patch.getTitle());
        if (patch.getAuthor() != null)    cur.setAuthor(patch.getAuthor());
        if (patch.getDate() != null)      cur.setDate(patch.getDate());
        if (patch.getCategory() != null)  cur.setCategory(patch.getCategory());
        if (patch.getImageURL() != null)  cur.setImageURL(patch.getImageURL());
        if (patch.getContent() != null)   cur.setContent(patch.getContent());
        cur.setDate(Instant.now().toString());

        await(ref.set(cur, SetOptions.merge()));
        return cur;
    }

    /* ===== 삭제 ===== */
    public void delete(String id) {
        await(firestore.collection(COL).document(id).delete());
    }
}
