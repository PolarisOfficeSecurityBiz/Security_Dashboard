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
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SecuNewsDao {

    private final Firestore firestore;
    private static final String COL = "SecuNews";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ISO  = DateTimeFormatter.ISO_INSTANT;

    private SecuNews map(DocumentSnapshot d) {
        SecuNews n = d.toObject(SecuNews.class);
        if (n != null) n.setId(d.getId());
        return n;
    }

    public List<SecuNews> findAll() throws ExecutionException, InterruptedException {
        // 문자열 ISO 정렬도 최신순이 맞으므로 updatedAt desc 정렬
        ApiFuture<QuerySnapshot> fut = firestore.collection(COL)
                .orderBy("updatedAt", Query.Direction.DESCENDING).get();
        return fut.get().getDocuments().stream().map(this::map).collect(Collectors.toList());
    }

    public SecuNews findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection(COL).document(id).get().get();
        return snap.exists() ? map(snap) : null;
    }

    public String create(SecuNews news) throws ExecutionException, InterruptedException {
        if (news.getCategory() == null || news.getCategory().isBlank()) news.setCategory("보안뉴스");
        if (news.getDate() == null || news.getDate().isBlank()) {
            news.setDate(LocalDate.now(KST).format(DATE));
        }
        news.setUpdatedAt(ISO.format(Instant.now()));

        DocumentReference ref = firestore.collection(COL).document();
        ref.set(news).get();
        return ref.getId();
    }

    public SecuNews update(String id, SecuNews patch) throws ExecutionException, InterruptedException {
        DocumentReference ref = firestore.collection(COL).document(id);
        SecuNews cur = findById(id);
        if (cur == null) throw new IllegalArgumentException("News not found: " + id);

        if (patch.getTitle() != null)     cur.setTitle(patch.getTitle());
        if (patch.getUrl() != null)       cur.setUrl(patch.getUrl());
        if (patch.getCategory() != null)  cur.setCategory(patch.getCategory());
        if (patch.getThumbnail() != null) cur.setThumbnail(patch.getThumbnail());
        if (patch.getDate() != null)      cur.setDate(patch.getDate());

        cur.setUpdatedAt(ISO.format(Instant.now()));
        ref.set(cur, SetOptions.merge()).get();
        return cur;
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COL).document(id).delete().get();
    }
}
