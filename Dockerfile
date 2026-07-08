# use JDK 21 to comile the Spring Boot application
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# copy Maven wrapper and project configs
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# make the Maven wrapper executable inside the Linux container
RUN chmod +x mvnw

# install dependencies
RUN ./mvnw dependency:go-offline

# copy application source code
COPY src src

# build the application jar
RUN ./mvnw clean package -DskipTests

# use jre image to run the built jar
FROM eclipse-temurin:21-jre

WORKDIR /app

# copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# expose port 8080
EXPOSE 8080

# run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
