# Build stage
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Development and runtime stage
FROM openjdk:17-jdk

# Set the working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/ensf400Project-1.0-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Install Maven
RUN apt-get update && apt-get install -y maven

# Install JMeter
RUN apt-get install -y wget unzip \
    && wget https://downloads.apache.org/jmeter/binaries/apache-jmeter-5.5.tgz \
    && tar -xzf apache-jmeter-5.5.tgz -C /opt \
    && rm apache-jmeter-5.5.tgz
ENV JMETER_HOME=/opt/apache-jmeter-5.5

# Install dependencies for OWASP Dependency Check
RUN apt-get install -y curl

# Create a non-root user for development
RUN groupadd -r vscode && useradd -r -g vscode -m -s /bin/bash vscode
# Make sure vscode user can access the application files
RUN mkdir -p /workspace && chown -R vscode:vscode /workspace /app

# Keep the ENTRYPOINT commented during development in Codespaces
# When deploying the application, uncomment this line
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Set the workspace directory for development
WORKDIR /workspace