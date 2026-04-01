# Multi-stage build for JHipster API Server
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle gradle.properties settings.gradle* ./
COPY buildSrc/ buildSrc/
RUN ./gradlew dependencies --no-daemon || true
COPY . .
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY src/main/docker/jib/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
COPY --from=build /app/build/libs/*.jar /app/app.jar
ENV JAVA_OPTS="-Xmx512m" JHIPSTER_SLEEP=0
EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
