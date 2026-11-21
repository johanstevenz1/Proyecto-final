# Imagen del backend de Jovan Pets para despliegue (por ejemplo, Render).
# Etapa 1: compila el JAR con Maven y JDK 21.
FROM maven:3.9.9-eclipse-temurin-21 AS compilacion
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Las pruebas se ejecutan en desarrollo (mvnw test); aqui solo se empaqueta.
RUN mvn -B -q -DskipTests package

# Etapa 2: imagen liviana solo con el JRE.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilacion /app/target/jovan-pets-backend-1.0.0.jar app.jar
# El proveedor define PORT; la aplicacion usa server.port=${PORT:8080}.
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
