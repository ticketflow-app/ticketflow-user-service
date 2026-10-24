FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]