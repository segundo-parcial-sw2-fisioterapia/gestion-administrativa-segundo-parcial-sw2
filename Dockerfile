# Etapa de compilación
FROM eclipse-temurin:24-jdk-alpine AS builder

WORKDIR /app

# Copiamos el wrapper de gradle y los archivos de configuración
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Damos permisos de ejecución al wrapper
RUN chmod +x gradlew

# Descargamos las dependencias de gradle para aprovechar el caché
RUN ./gradlew dependencies --no-daemon || true

# Copiamos el código fuente y el archivo .env
COPY src ./src
COPY .env .env

# Compilamos el .jar omitiendo los tests para que sea más rápido
RUN ./gradlew build -x test --no-daemon

# Etapa de producción
FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

# Copiamos el .jar generado desde la etapa builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Copiamos el .env dentro de la imagen final (tal como lo pidieron)
COPY --from=builder /app/.env .env

EXPOSE 3001

# Ejecutamos la aplicación
CMD ["java", "-jar", "app.jar"]
