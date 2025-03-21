FROM ubuntu:latest
LABEL authors="petertogara"

FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/wallet-service*.jar app.jar
COPY wallet-keystore.p12 /app/keystore.p12

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

