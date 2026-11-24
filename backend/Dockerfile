# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S alocar && adduser -S alocar -G alocar
USER alocar
WORKDIR /app
COPY --from=build /app/target/alocar-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
