# syntax=docker/dockerfile:1.7
#
# Multi-stage Dockerfile for the Spring Boot backend.
#
# Stage 1 builds the executable jar with the Gradle wrapper on a full JDK
# image. We cache the dependency download as its own layer so that day-to-day
# code edits don't re-download the world on every build.
#
# Stage 2 ships only the resulting jar on a slim JRE image, dropping the
# build tools to keep the image small (~250 MB).
#
# Build locally:
#   docker build -t pharmacie-backend ./backend
# Run locally:
#   docker run --rm -p 8080:8080 \
#       -e DB_URL=jdbc:postgresql://host.docker.internal:5432/pharmacie \
#       -e DB_USER=pharmacie -e DB_PASSWORD=pharmacie \
#       -e ADMIN_USERNAME=admin -e ADMIN_PASSWORD=changeme \
#       pharmacie-backend
# -----------------------------------------------------------------------------

# ---- Stage 1 : build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy only what's needed for the wrapper + dependency resolution first
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Now copy the rest of the source and build the fat jar
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Stage 2 : runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Drop privileges — never run a Java server as root.
RUN addgroup -S app && adduser -S -G app app
USER app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080

# Use exec form so signals (SIGTERM from Docker) reach the JVM cleanly.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]

