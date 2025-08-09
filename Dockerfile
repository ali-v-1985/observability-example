# Use Maven image for building
FROM maven:3.9.6-openjdk-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Use OpenJDK 21 for runtime
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/observability-example-0.0.1-SNAPSHOT.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "app.jar"]
