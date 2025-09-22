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
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
