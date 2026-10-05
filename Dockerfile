FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -B clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/student-topic-management.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
