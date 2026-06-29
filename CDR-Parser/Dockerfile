FROM eclipse-temurin:21-jre
WORKDIR /app

COPY target/CDR-Parser-1.0-SNAPSHOT.jar app.jar
COPY io.properties .

ENTRYPOINT ["java", "-jar", "app.jar"]
