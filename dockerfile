FROM eclipse-temurin:21-jdk-alpine

RUN apk update && \
    apk add --no-cache tar unzip bash && \
    rm -rf /var/cache/apk/*

WORKDIR /app

EXPOSE 8080
