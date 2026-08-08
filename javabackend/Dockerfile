# Stage 1: Build Maven application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache su-exec \
    && addgroup -S appgroup && adduser -S -G appgroup -u 10001 appuser \
    && mkdir -p /app/data/uploads \
    && chown -R appuser:appgroup /app

# Copy built jar from build stage
COPY --from=build --chown=appuser:appgroup /app/target/javabackend-0.0.1-SNAPSHOT.jar app.jar

# Copy entrypoint script
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

# Do NOT use USER appuser here. We must start as root to chown Fargate volumes.
# The entrypoint script will drop privileges using su-exec.

# Expose port 8080
EXPOSE 8080

# Run Spring Boot application via entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
