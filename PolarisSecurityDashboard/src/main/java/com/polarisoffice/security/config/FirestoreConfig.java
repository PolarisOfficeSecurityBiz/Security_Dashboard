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

  // gcp.project-id 우선, 없으면 spring.cloud.gcp.project-id
  @Value("${gcp.project-id:${spring.cloud.gcp.project-id:}}")
  private String projectId;

  // gcp.credentials.location 우선, 없으면 spring.cloud.gcp.credentials.location
  @Value("${gcp.credentials.location:${spring.cloud.gcp.credentials.location:}}")
  private Resource credentialsLocation;

  @Bean
  public Firestore firestore() throws IOException {
    GoogleCredentials credentials;

    if (credentialsLocation != null && credentialsLocation.exists()) {
      try (InputStream is = credentialsLocation.getInputStream()) {
        credentials = GoogleCredentials.fromStream(is);
      }
    } else {
      // 최후: ADC (환경변수 GOOGLE_APPLICATION_CREDENTIALS, gcloud ADC, GCP 메타데이터)
      credentials = GoogleCredentials.getApplicationDefault();
    }

    FirestoreOptions.Builder builder = FirestoreOptions.newBuilder().setCredentials(credentials);
    if (projectId != null && !projectId.isBlank()) {
      builder.setProjectId(projectId);
    }
    return builder.build().getService();
  }
}
