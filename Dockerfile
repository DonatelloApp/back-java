# Etapa 1: Construcción de la aplicación con Maven
FROM maven:3.8.4-openjdk-11-slim AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Etapa 2: Preparación de la imagen de producción
FROM openjdk:11-jre-slim AS runner
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
