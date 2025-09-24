# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
# 모듈명이 PolarisSecurityDashboard 라는 전제 (pom.xml 다모듈이면 -pl/-am 사용)
RUN mvn -B -pl PolarisSecurityDashboard -am clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# 만들어진 JAR 경로 확인 후 필요하면 파일명/경로 조정
COPY --from=build /src/PolarisSecurityDashboard/target/*-SNAPSHOT.jar /app/app.jar
# 포트는 앱에 맞게
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]