FROM openjdk:10.0-jdk-sid as builder
ADD src/ src
ADD build.gradle build.gradle
ADD gradlew gradlew
ADD gradle/ gradle
RUN ./gradlew bootJar

FROM registry.kloud.software/admin/debianbase
WORKDIR /root/
COPY --from=builder /build/libs/0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/application-prod.properties application.properties
ENTRYPOINT ["bash", "-c", "java -jar /root/app.jar"]
