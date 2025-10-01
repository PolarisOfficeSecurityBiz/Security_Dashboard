package com.polarisoffice.security.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FirestoreConfig {

  private static final Logger log = LoggerFactory.getLogger(FirestoreConfig.class);

  // 1순위 gcp.project-id, 2순위 spring.cloud.gcp.project-id
  @Value("${gcp.project-id:${spring.cloud.gcp.project-id:}}")
  private String projectId;

  // 1순위 gcp.credentials.location, 2순위 spring.cloud.gcp.credentials.location
  // 예) classpath:firebase-service-account.json 또는 file:/etc/app/firebase.json
  @Value("${gcp.credentials.location:${spring.cloud.gcp.credentials.location:}}")
  private Resource credentialsLocation;

  @Bean
  public Firestore firestore() throws IOException {
    GoogleCredentials credentials;
    String origin;

    // 1) 명시 파일 우선
    if (credentialsLocation != null && credentialsLocation.exists()) {
      origin = credentialsLocation.toString();
      try (InputStream is = credentialsLocation.getInputStream()) {
        credentials = GoogleCredentials.fromStream(is);
      } catch (IOException e) {
        throw new IllegalStateException("자격증명 파일을 열 수 없습니다: " + origin, e);
      }
    } else {
      // 2) ADC (환경변수 GOOGLE_APPLICATION_CREDENTIALS / gcloud / GCE 메타데이터)
      try {
        credentials = GoogleCredentials.getApplicationDefault();
        origin = "ADC";
      } catch (IOException e) {
        throw new IllegalStateException(
            "GCP 자격증명을 찾을 수 없습니다. " +
            "gcp.credentials.location(=classpath:/ or file:/) 또는 환경변수 GOOGLE_APPLICATION_CREDENTIALS를 설정하세요.", e);
      }
    }

    // 일부 서버 환경은 스코프 명시 필요
    if (credentials.createScopedRequired()) {
      credentials = credentials.createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
    }

    FirestoreOptions.Builder builder = FirestoreOptions.newBuilder().setCredentials(credentials);
    if (projectId != null && !projectId.isBlank()) {
      builder.setProjectId(projectId);
    }
    FirestoreOptions options = builder.build();

    log.info("✅ Firestore 초기화: projectId={}, credsOrigin={}", options.getProjectId(), origin);
    return options.getService();
  }
}
