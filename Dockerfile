FROM eclipse-temurin:26-jre-jammy
WORKDIR /app
COPY backend/build/install/backend/ .
COPY site/build/processedResources/js/main/public/ /app/public/
COPY site/build/kotlin-webpack/js/productionExecutable/stackmanager.js /app/public/stackmanager.js
COPY site/build/kotlin-webpack/js/productionExecutable/stackmanager.js.map /app/public/stackmanager.js.map
EXPOSE 8080
ENV STACKMGR_BIND_HOST=0.0.0.0
ENV STACKMGR_PORT=8080
ENV STACKMGR_WEB_ROOT=/app/public
CMD ["bin/backend"]
