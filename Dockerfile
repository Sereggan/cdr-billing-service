# The jar is built outside of the image, see the README for the required build step.
FROM eclipse-temurin:21-jre

COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
