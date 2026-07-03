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

# Create directory for persistent image uploads
RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# Copy built jar from build stage
COPY --from=build /app/target/javabackend-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
