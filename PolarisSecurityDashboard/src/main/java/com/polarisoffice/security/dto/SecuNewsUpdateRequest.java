package com.polarisoffice.security.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SecuNewsUpdateRequest {
    private String title;
    private String url;
    private String category;
    private String thumbnail;
    private String date;       // "YYYY-MM-DD"
}
