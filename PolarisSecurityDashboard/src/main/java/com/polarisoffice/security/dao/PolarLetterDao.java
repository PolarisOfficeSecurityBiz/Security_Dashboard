package com.polarisoffice.security.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.polarisoffice.security.model.PolarLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class PolarLetterDao {

    private final Firestore firestore;
    private static final String COL = "polarLetter";

    /** ApiFuture 공통 대기 */
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

    private PolarLetter map(DocumentSnapshot d) {
        PolarLetter n = d.toObject(PolarLetter.class);
        if (n != null) n.setId(d.getId());
        return n;
    }

    /** 목록 – create_time DESC */
    public List<PolarLetter> findAll(int size, String q) {
        Query query = firestore.collection(COL)
                .orderBy("create_time", Query.Direction.DESCENDING);
        if (size > 0) query = query.limit(size);

        List<QueryDocumentSnapshot> docs = await(query.get()).getDocuments();
        List<PolarLetter> rows = docs.stream().map(this::map).toList();

        // 서버측 필터
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            return rows.stream().filter(n ->
                    (n.getTitle() != null && n.getTitle().toLowerCase().contains(needle)) ||
                    (n.getAuthor() != null && n.getAuthor().toLowerCase().contains(needle)) ||
                    (n.getUrl() != null && n.getUrl().toLowerCase().contains(needle)) ||
                    (n.getContent() != null && n.getContent().toLowerCase().contains(needle))
            ).toList();
        }
        return rows;
    }

    /** 단건 */
    public Optional<PolarLetter> findById(String id) {
        DocumentSnapshot snap = await(firestore.collection(COL).document(id).get());
        return snap.exists() ? Optional.of(map(snap)) : Optional.empty();
    }

    /** 생성 */
    public PolarLetter create(PolarLetter n) {
        if (n.getUpdatedAt() == null) n.setUpdatedAt(Instant.now().toString());
        DocumentReference ref = firestore.collection(COL).document();
        n.setId(ref.getId());
        await(ref.set(n));
        return n;
    }

    /** 수정 */
    public PolarLetter update(String id, PolarLetter patch) {
        DocumentReference ref = firestore.collection(COL).document(id);
        PolarLetter cur = findById(id).orElseThrow(() -> new IllegalArgumentException("PolarLetter not found: " + id));

        if (patch.getTitle() != null)      cur.setTitle(patch.getTitle());
        if (patch.getAuthor() != null)     cur.setAuthor(patch.getAuthor());
        if (patch.getContent() != null)    cur.setContent(patch.getContent());
        if (patch.getThumbnail() != null)  cur.setThumbnail(patch.getThumbnail());
        if (patch.getUrl() != null)        cur.setUrl(patch.getUrl());
        if (patch.getCreateTime() != null) cur.setCreateTime(patch.getCreateTime());
        cur.setUpdatedAt(Instant.now().toString());

        await(ref.set(cur, SetOptions.merge()));
        return cur;
    }

    /** 삭제 */
    public void delete(String id) {
        await(firestore.collection(COL).document(id).delete());
    }

    /** 전체 개수 */
    public int count() {
        CollectionReference col = firestore.collection(COL);
        List<QueryDocumentSnapshot> docs = await(col.get()).getDocuments();
        return docs.size();
    }
}
