# ---------- Stage 1: build the WAR with Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
# Download dependencies first (cached layer unless pom.xml changes)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:17-jre-jammy
ARG APP_VERSION=local
LABEL org.opencontainers.image.title="Parking Violation Appeal Portal" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.description="DevOps mini-project - parking violation appeal portal"

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --uid 1001 --create-home appuser \
 && mkdir -p /app/data \
 && chown -R appuser /app

WORKDIR /app
COPY --from=build /src/target/parking-appeal.war /app/app.war

ENV APP_ENV=docker \
    APP_DATA_DIR=/app/data \
    APP_BUILD=${APP_VERSION} \
    JAVA_OPTS="-Xms128m -Xmx384m"

USER appuser
EXPOSE 8080
VOLUME ["/app/data"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fs http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.war"]
