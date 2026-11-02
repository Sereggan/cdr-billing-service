# Build stage: compiles the application inside the image, so a clean checkout only needs Docker.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Resolve dependencies in a separate layer so they are cached between source changes.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q -DskipTests package

# Runtime stage: JRE only, runs as a non-root user.
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 app
COPY --from=build /workspace/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
