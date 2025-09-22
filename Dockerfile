<<<<<<< Updated upstream
# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
# 의존성 캐시
COPY PolarisSecurityDashboard/pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
# 소스 복사 & 빌드
COPY PolarisSecurityDashboard/src ./src
RUN mvn -B -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
=======
# -------- runtime only --------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# GitHub Actions 빌드에서 만들어진 JAR을 복사
COPY PolarisSecurityDashboard/target/*.jar app.jar
>>>>>>> Stashed changes
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
