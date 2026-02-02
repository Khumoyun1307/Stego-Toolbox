FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw || true
RUN ./mvnw -B -pl stego-api -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/stego-api/target/*.jar /app/app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

