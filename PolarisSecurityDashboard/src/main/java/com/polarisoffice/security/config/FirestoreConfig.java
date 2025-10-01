package com.polarisoffice.security.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
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
    String origin;

    if (credentialsLocation != null && credentialsLocation.exists()) {
      try (InputStream is = credentialsLocation.getInputStream()) {
        credentials = GoogleCredentials.fromStream(is);
        origin = credentialsLocation.getURI().toString();
      }
    } else {
      credentials = GoogleCredentials.getApplicationDefault();
      origin = "ADC(Application Default Credentials)";
    }

    FirestoreOptions.Builder builder = FirestoreOptions.newBuilder().setCredentials(credentials);
    if (StringUtils.hasText(projectId)) {
      builder.setProjectId(projectId);
    }

    Firestore fs = builder.build().getService();
    log.info("✅ Firestore initialized. projectId={}, credsOrigin={}", 
             fs.getOptions().getProjectId(), origin);
    return fs;
  }
}
