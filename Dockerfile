FROM gradle:9.4.1 AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY backend/ backend/
COPY site/ site/
RUN chmod +x gradlew && ./gradlew :site:jsBrowserProductionWebpack :backend:installDist --no-daemon

FROM eclipse-temurin:26-jre-jammy
WORKDIR /app
COPY --from=builder /app/backend/build/install/backend/ .
COPY --from=builder /app/site/build/dist/js/productionExecutable/public/ /app/public/
COPY --from=builder /app/site/build/dist/js/productionExecutable/stackmanager.js /app/public/stackmanager.js
COPY --from=builder /app/site/build/dist/js/productionExecutable/stackmanager.js.map /app/public/stackmanager.js.map
EXPOSE 8080
ENV STACKMGR_BIND_HOST=127.0.0.1
ENV STACKMGR_PORT=8080
ENV STACKMGR_WEB_ROOT=/app/public
CMD ["bin/backend"]
