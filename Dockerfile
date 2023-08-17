# Dependency images
FROM yti-spring-security:latest as yti-spring-security

# Builder image
FROM adoptopenjdk/maven-openjdk11 as builder

# Copy yti-spring-security dependency from MAVEN repo
COPY --from=yti-spring-security /root/.m2/repository/fi/vm/yti/ /root/.m2/repository/fi/vm/yti/

# Set working dir
WORKDIR /app

# Copy source file
COPY src src
COPY pom.xml .

# Build project
RUN mvn clean package -DskipTests

# Pull base image
FROM yti-docker-java11-base:alpine

# Copy from builder 
COPY --from=builder /app/target/yti-comments-api.jar ${deploy_dir}/yti-comments-api.jar

# Expose port
EXPOSE 9701

# Set default command on run
ENTRYPOINT ["/bootstrap.sh", "yti-comments-api.jar"]
