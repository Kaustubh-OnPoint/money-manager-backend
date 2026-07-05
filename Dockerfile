# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/moneymanager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]