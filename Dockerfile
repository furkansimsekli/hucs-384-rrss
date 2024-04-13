FROM eclipse-temurin:21-jre-alpine

RUN mkdir -p /app/data
WORKDIR /app
COPY build/libs/*.jar /app/app.jar
EXPOSE 8080

VOLUME /app/data
ENTRYPOINT ["java","-jar","/app/app.jar"]
