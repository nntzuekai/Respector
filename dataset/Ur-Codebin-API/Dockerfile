FROM maven:3.6.3-openjdk-11-slim AS builder
WORKDIR /
COPY pom.xml .
COPY src ./src/
RUN mvn package

FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /
COPY --from=builder ./target/ur-codebin-api-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-XX:+PrintFlagsFinal", "-Xmx150m", "-Dspring.profiles.active=prod", "-jar", "app.jar"]