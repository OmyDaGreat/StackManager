FROM gradle:9.4.1 AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY backend/ backend/
COPY site/ site/
RUN chmod +x gradlew && ./gradlew :backend:installDist --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/backend/build/install/backend/ .
EXPOSE 8080
ENV STACKMGR_BIND_HOST=127.0.0.1
ENV STACKMGR_PORT=8080
CMD ["bin/backend"]
