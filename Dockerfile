FROM registry.kloud.software/debianbase:stretch
WORKDIR /root/
COPY build/libs/silver-1.0.jar app.jar
COPY src/main/resources/application-prod.properties application.properties
ENTRYPOINT ["java", "-jar", "/root/app.jar"]
