# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# POM 먼저 복사해 의존성 캐시
COPY pom.xml ./pom.xml
COPY PolarisSecurityDashboard/pom.xml ./PolarisSecurityDashboard/pom.xml

# 의존성 선반영(캐시)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -e -U -DskipTests -DskipITs -Dmaven.test.skip=true \
        --no-transfer-progress dependency:go-offline

# 실제 소스
COPY . .

# 패키징(테스트 완전 스킵)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -e -U -pl PolarisSecurityDashboard -am \
        clean package \
        -DskipTests -DskipITs -Dmaven.test.skip=true \
        --no-transfer-progress

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# 산출물 이름 변동에 안전하게 패턴 복사
COPY --from=build /workspace/PolarisSecurityDashboard/target/*jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
