# syntax=docker/dockerfile:1

# ---- Build stage --------------------------------------------------
# Uses the full JDK to compile the project and produce the executable
# jar via the Gradle wrapper. Discarded after the build completes.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy the Gradle wrapper and build files first so that this layer
# stays cached as long as the build configuration does not change.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Pre-fetch dependencies into the Gradle cache (best-effort — fails
# silently when offline mirrors are unreachable so the actual build
# can still try). Speeds up subsequent rebuilds when only sources change.
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Now copy sources and build the jar. Tests are skipped here because
# they are run separately in CI; uncomment the next line to run them
# inside the image build instead.
COPY src ./src
# RUN ./gradlew --no-daemon test
RUN ./gradlew --no-daemon bootJar

# ---- Runtime stage ------------------------------------------------
# Slim JRE-only image — the JDK and Gradle build artifacts are not
# carried over, so the final image is much smaller.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy just the fat jar produced by Spring Boot.
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
