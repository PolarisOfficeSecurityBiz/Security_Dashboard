package com.polarisoffice.security.model;

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
	private String updateAt;
	
}
