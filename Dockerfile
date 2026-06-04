FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
ENV TZ=Asia/Shanghai
RUN addgroup -S bookstore && adduser -S bookstore -G bookstore
COPY --from=build /workspace/target/bookstore-1.0.0.jar /app/app.jar
USER bookstore

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
