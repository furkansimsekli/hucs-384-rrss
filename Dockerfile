FROM gradle:8-jdk21 as builder
LABEL stage=builder

COPY . /repo
WORKDIR /repo
RUN gradle build -x test

FROM eclipse-temurin:21-jre-alpine

RUN mkdir -p /app/data
WORKDIR /app
COPY --from=builder /repo/build/libs/*.jar /app/app.jar
EXPOSE 8080

VOLUME /app/data
ENTRYPOINT ["java","-jar","/app/app.jar"]
