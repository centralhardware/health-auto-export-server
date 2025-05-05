FROM gradle:jdk23-graal as gradle
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM openjdk:23-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
