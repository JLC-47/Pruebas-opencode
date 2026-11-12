# syntax=docker/dockerfile:1

FROM node:24-alpine AS frontend

WORKDIR /frontend
COPY src/main/resources/frontend/package.json src/main/resources/frontend/package-lock.json ./
RUN npm ci
COPY src/main/resources/frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
COPY --from=frontend /static ./src/main/resources/static
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring spring

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

USER spring
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
