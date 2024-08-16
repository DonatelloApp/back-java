# Etapa 1: Construcción de la aplicación con Maven
FROM maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Etapa 2: Preparación de la imagen de producción
FROM openjdk:17-jdk-slim AS runner

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV DATASOURCE_PROD_DONATELLO_RENDER=${DATASOURCE_PROD_DONATELLO_RENDER}
ENV USER_PROD_DONATELLO_RENDER=${USER_PROD_DONATELLO_RENDER}
ENV PASSWORD_PROD_DONATELLO_RENDER=${PASSWORD_PROD_DONATELLO_RENDER}

# Limpieza de artefactos de construcción
RUN rm -rf /app/src \
    && rm -rf /root/.m2

CMD ["java", "-jar", "app.jar"]
