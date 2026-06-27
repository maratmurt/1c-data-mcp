# Build from repo root: docker build -f docker/Dockerfile -t 1c-data-mcp-server .
# Or from docker/:              docker build -f Dockerfile -t 1c-data-mcp-server ..

FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY server/build.gradle.kts server/settings.gradle.kts ./
COPY server/src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
COPY --from=build /app/build/libs/1c-data-mcp-server.jar app.jar
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-jar", "app.jar"]
