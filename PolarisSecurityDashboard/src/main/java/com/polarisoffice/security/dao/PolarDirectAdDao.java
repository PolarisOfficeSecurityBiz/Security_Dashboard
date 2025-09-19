package com.polarisoffice.security.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.polarisoffice.security.model.PolarDirectAd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PolarDirectAdDao {

  private final Firestore firestore;
  private static final String COL = "PolarDirectAds";

  public List<PolarDirectAd> findAll() {
    try {
      ApiFuture<QuerySnapshot> fut = firestore.collection(COL).get();
      return fut.get().getDocuments().stream()
    		    .map(d -> {
    		        PolarDirectAd ad = d.toObject(PolarDirectAd.class);
    		        if (ad != null) ad.setId(d.getId());   // 반드시 문서 ID 주입
    		        return ad;
    		    }).collect(Collectors.toList());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while fetching all ads", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to fetch all ads", e);
    }
  }

  public PolarDirectAd findById(String id) {
    try {
      DocumentSnapshot snap = firestore.collection(COL).document(id).get().get();
      PolarDirectAd ad = snap.toObject(PolarDirectAd.class);
      if (ad != null) ad.setId(snap.getId());
     
      return snap.exists() ? snap.toObject(PolarDirectAd.class) : null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while fetching ad: " + id, e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to fetch ad: " + id, e);
    }
  }

  public String create(PolarDirectAd ad) {
	  try {
		    ad.setPublishedDate(Timestamp.now());
		    ad.setUpdatedAt(Timestamp.now());     // ← 통일
		    DocumentReference ref = firestore.collection(COL).document();
		    ref.set(ad).get();
		    return ref.getId();
		  } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while creating ad", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to create ad", e);
    }
  }

  public PolarDirectAd update(String id, PolarDirectAd patch) {
	  try {
	    DocumentReference ref = firestore.collection(COL).document(id);
	    DocumentSnapshot snap = ref.get().get();
	    if (!snap.exists()) throw new IllegalArgumentException("Ad not found: " + id);

	    PolarDirectAd cur = snap.toObject(PolarDirectAd.class);
	    if (cur == null) throw new IllegalStateException("Mapping failed: " + id);

	    if (patch.getAdType() != null) cur.setAdType(patch.getAdType());
	    if (patch.getAdvertiserName() != null) cur.setAdvertiserName(patch.getAdvertiserName());
	    if (patch.getBackgroundColor() != null) cur.setBackgroundColor(patch.getBackgroundColor());
	    if (patch.getImageUrl() != null) cur.setImageUrl(patch.getImageUrl());
	    if (patch.getTargetUrl() != null) cur.setTargetUrl(patch.getTargetUrl());
	    if (patch.getClickCount() != null) cur.setClickCount(patch.getClickCount());
	    if (patch.getViewCount() != null) cur.setViewCount(patch.getViewCount());

	    cur.setUpdatedAt(Timestamp.now());   // ← 통일

	    ref.set(cur, SetOptions.merge()).get();
	    return cur;
	  }  catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while updating ad: " + id, e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to update ad: " + id, e);
    }
  }

  public void delete(String id) {
    try {
      firestore.collection(COL).document(id).delete().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while deleting ad: " + id, e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to delete ad: " + id, e);
    }
  }
}
