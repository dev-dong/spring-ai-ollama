FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon

COPY src ./src
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xmx512m", "-Xms512m", "-XX:MaxMetaspaceSize=128m", "-XX:+UseG1GC", "-jar", "app.jar"]