package com.polarisoffice.security.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import com.google.cloud.firestore.annotation.PropertyName;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.*;

//PolarDirectAd.java
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@IgnoreExtraProperties
public class PolarDirectAd {
 @DocumentId
 private String id;

 private AdType adType;                 // ← enum 타입
 private String advertiserName;
 private String backgroundColor;
 private String imageUrl;
 private String targetUrl;
 private Long clickCount;
 private Long viewCount;
 private Timestamp publishedDate;
 @ServerTimestamp
 private Timestamp updatedAt;

 // (Firestore 문서에 과거 updateAt만 있을 때 호환)
 @PropertyName("updateAt") public void setUpdateAtLegacy(Timestamp t){ this.updatedAt = t; }
 @PropertyName("updateAt") public Timestamp getUpdateAtLegacy(){ return this.updatedAt; }
}
