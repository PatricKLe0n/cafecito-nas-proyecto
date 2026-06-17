# Imagen para desplegar el backend (que ya sirve el frontend compilado) en un hosting.
# No se usa en local; en local basta con `mvn package` + `java -jar`.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/trazabilidad-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
