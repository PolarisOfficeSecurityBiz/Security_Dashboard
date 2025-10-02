package com.polarisoffice.security.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
// ★ 이걸 import 해야 gRPC 제공자를 쓸 수 있어요
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class FirestoreConfig {

  @Value("${gcp.project-id:${spring.cloud.gcp.project-id:}}")
  private String projectId;

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
      credentials = GoogleCredentials.getApplicationDefault();
    }

    FirestoreOptions.Builder builder = FirestoreOptions.getDefaultInstance().toBuilder()
        .setCredentials(credentials)
        // ★ gRPC를 setChannelProvider로 강제 (HttpJson 절대 금지)
        .setChannelProvider(
            InstantiatingGrpcChannelProvider.newBuilder()
                .setEndpoint("firestore.googleapis.com:443")
                .build()
        );

    if (projectId != null && !projectId.isBlank()) {
      builder.setProjectId(projectId);
    }

    return builder.build().getService();
  }
}
