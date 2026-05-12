FROM eclipse-temurin:26-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends docker.io && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY backend/build/install/backend/ .
COPY site/build/processedResources/js/main/public/ /app/public/
COPY site/build/kotlin-webpack/js/productionExecutable/stackmanager.js /app/public/stackmanager.js
COPY site/build/kotlin-webpack/js/productionExecutable/stackmanager.js.map /app/public/stackmanager.js.map
EXPOSE 8080
ENV STACKMGR_BIND_HOST=0.0.0.0
ENV STACKMGR_PORT=8080
ENV STACKMGR_WEB_ROOT=/app/public
ENV STACKMGR_DOCKER_BIN=/usr/bin/docker
ENV STACKMGR_DOCKER_HOST=unix:///var/run/docker.sock
CMD ["bin/backend"]
