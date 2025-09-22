# ---------- build stage ----------
FROM gradle:8.9-jdk21-alpine AS build
WORKDIR /workspace
COPY build.gradle settings.gradle gradle.properties* ./
COPY gradle gradle
RUN gradle --no-daemon build -x test || true  # 의존성 캐시용 프리빌드
COPY . .
RUN gradle --no-daemon clean bootJar

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 빌드 산출물 jar 경로는 프로젝트 구조에 맞게 조정
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
