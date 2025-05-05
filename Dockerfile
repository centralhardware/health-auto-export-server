FROM gradle:jdk21-graal as build
WORKDIR /app
COPY . .
RUN gradle installDist --no-daemon

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/build/install/health-auto-export-server /app
EXPOSE 8080

CMD ["/app/bin/health-auto-export-server"]
