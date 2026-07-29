# Build the Spring Boot application with JDK 21
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy dependency files first so Docker can reuse cached layers
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the Maven wrapper executable inside the Linux container
RUN chmod +x mvnw

# Download dependencies before copying the source code
RUN ./mvnw dependency:go-offline

# Copy the application source code
COPY src src

# Build the executable JAR without rerunning tests
RUN ./mvnw clean package -DskipTests

# Run the application with a smaller JRE image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
