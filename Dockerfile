# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
# 전체 모듈을 통째로 복사(멀티모듈/추가 파일 누락 방지)
COPY PolarisSecurityDashboard/ .
# 의존성/빌드
RUN mvn -B -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 결과 JAR 복사 (target/*.jar)
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
