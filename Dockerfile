# Build the React frontend independently.
FROM node:22-bookworm-slim AS frontend-build
WORKDIR /app/frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY frontend/ ./
RUN npm run build

# Build the Spring Boot backend. The built frontend is copied into the backend
# resources by Maven, so both are served from the same origin in production.
FROM maven:3.9.16-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY backend/ ./backend/
COPY --from=frontend-build /app/frontend/dist ./frontend/dist
COPY --from=frontend-build /app/frontend/dist/ ./backend/src/main/resources/static/

WORKDIR /app/backend
RUN mvn -B -DskipTests -Dfrontend.skip=true clean package

# Small production runtime image.
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=backend-build /app/backend/target/studysync-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
