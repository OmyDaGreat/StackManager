FROM eclipse-temurin:26-jre-jammy
WORKDIR /app
COPY backend/build/install/backend/ .
COPY site/build/dist/js/productionExecutable/public/ /app/public/
COPY site/build/dist/js/productionExecutable/stackmanager.js /app/public/stackmanager.js
EXPOSE 8080
ENV STACKMGR_BIND_HOST=127.0.0.1
ENV STACKMGR_PORT=8080
ENV STACKMGR_WEB_ROOT=/app/public
CMD ["bin/backend"]
