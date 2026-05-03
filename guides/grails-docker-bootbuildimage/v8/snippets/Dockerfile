# syntax=docker/dockerfile:1.7
#
# Hand-rolled multi-stage Dockerfile (alternative to bootBuildImage).
# Build with:    docker build -t grails-docker-bootbuildimage:dockerfile .
# Run with:      docker run --rm -p 8080:8080 grails-docker-bootbuildimage:dockerfile
#
# Use ./gradlew bootBuildImage instead unless you have a specific
# reason to control every layer (regulated builds, custom base image,
# air-gapped builds). See guide chapter `dockerfile.adoc`.

# ----------------------------------------------------------------------
# Build stage: compile + bootJar
# ----------------------------------------------------------------------
FROM bellsoft/liberica-openjdk-debian:21 AS build

WORKDIR /workspace
COPY gradlew gradlew.bat /workspace/
COPY gradle /workspace/gradle/
COPY build.gradle settings.gradle gradle.properties /workspace/
COPY grails-app /workspace/grails-app/
COPY src /workspace/src/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon bootJar -x test

# ----------------------------------------------------------------------
# Runtime stage: minimal JRE + the bootJar
# ----------------------------------------------------------------------
FROM bellsoft/liberica-openjre-debian:21 AS runtime

# Run as a non-root user so a container break-out cannot become a host
# escalation. UID 10001 has no /etc/passwd entry by design.
RUN groupadd --system --gid 10001 grails \
 && useradd  --system --uid 10001 --gid grails --no-create-home --shell /sbin/nologin grails

WORKDIR /app
COPY --from=build --chown=grails:grails /workspace/build/libs/*.jar /app/app.jar

USER 10001:10001
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS='-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport'

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=12 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness | grep -q UP || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
