FROM gradle:jdk22-graal as build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM openjdk:22-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
