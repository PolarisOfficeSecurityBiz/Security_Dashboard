# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# 모듈 POM만 먼저 복사 (루트 POM이 없다고 가정)
COPY PolarisSecurityDashboard/pom.xml PolarisSecurityDashboard/pom.xml

# 의존성 선반영
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -e -U \
        -f PolarisSecurityDashboard/pom.xml \
        -DskipTests -DskipITs -Dmaven.test.skip=true \
        --no-transfer-progress dependency:go-offline

# 실제 소스 복사 (모듈만)
COPY PolarisSecurityDashboard/ PolarisSecurityDashboard/

# 패키징
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -e -U \
        -f PolarisSecurityDashboard/pom.xml \
        clean package \
        -DskipTests -DskipITs -Dmaven.test.skip=true \
        --no-transfer-progress

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/PolarisSecurityDashboard/target/*jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
