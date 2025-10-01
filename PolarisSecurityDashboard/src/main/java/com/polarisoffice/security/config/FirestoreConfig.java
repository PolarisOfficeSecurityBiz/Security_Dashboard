package com.polarisoffice.security.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.v1.stub.FirestoreStubSettings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
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

    // ✅ HTTP/JSON 트랜스포트 강제
    TransportChannelProvider httpJson =
        FirestoreStubSettings.defaultHttpJsonTransportProviderBuilder().build();

    FirestoreOptions.Builder builder = FirestoreOptions.newBuilder()
        .setCredentials(credentials)
        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
        .setChannelProvider(httpJson);

    if (projectId != null && !projectId.isBlank()) {
      builder.setProjectId(projectId);
    }
    return builder.build().getService();
  }
}