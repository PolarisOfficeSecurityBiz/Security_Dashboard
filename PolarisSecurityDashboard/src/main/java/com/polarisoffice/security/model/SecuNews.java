package com.polarisoffice.security.model;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import com.google.cloud.Timestamp;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SecuNews {
	private String id;
	private String category;
	private String title;
	private String url;
	private String thumbnail;
	private String date;
	@ServerTimestamp
	private Timestamp updateAt;
	
}
