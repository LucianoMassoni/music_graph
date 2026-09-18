#BUILD STAGE
FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# RUN STAGE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/music_graph-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]