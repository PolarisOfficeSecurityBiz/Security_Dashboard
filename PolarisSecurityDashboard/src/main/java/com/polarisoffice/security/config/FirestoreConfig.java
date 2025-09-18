package com.polarisoffice.security.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class FirestoreConfig {

  @Value("${gcp.project-id:}")
  private String projectId;

  // Resource 타입으로 받으면 file:, classpath: 모두 처리 가능
  @Value("${gcp.credentials.location:}")
  private Resource credentialsLocation;

  @Bean
  public Firestore firestore() throws IOException {
    GoogleCredentials credentials;

    if (credentialsLocation != null && credentialsLocation.exists()) {
      try (InputStream is = credentialsLocation.getInputStream()) {
        credentials = GoogleCredentials.fromStream(is);
      }
    } else {
      // 환경변수 GOOGLE_APPLICATION_CREDENTIALS 또는 GCP 런타임 기본자격증명 사용
      credentials = GoogleCredentials.getApplicationDefault();
    }

    FirestoreOptions.Builder builder = FirestoreOptions.newBuilder().setCredentials(credentials);
    if (projectId != null && !projectId.isBlank()) {
      builder.setProjectId(projectId);
    }
    return builder.build().getService();
  }
}