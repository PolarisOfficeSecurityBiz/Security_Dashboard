package com.polarisoffice.security.model;

import com.google.cloud.Timestamp;
import lombok.*;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor @Builder
public class PolarDirectAd {
  @DocumentId
  private String id;                 // 문서 ID 자동 주입

  private AdType adType;             // BOTTOM/EVENT/EVENT_FAB/BANNER
  private String advertiserName;
  private String backgroundColor;    // default: #FFFFFF
  private Long clickCount;
  private String imageUrl;
  private Timestamp publishedDate;
  private String targetUrl;
  private Timestamp updateAt;
  private Long viewCount;
}