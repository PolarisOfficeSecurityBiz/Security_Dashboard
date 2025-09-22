# -------- runtime only --------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# GitHub Actions 빌드에서 만들어진 JAR을 복사
COPY PolarisSecurityDashboard/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
