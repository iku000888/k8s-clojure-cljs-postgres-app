FROM openjdk:11-jre-slim
WORKDIR /app
COPY ./target/*-standalone.jar ./app.jar
CMD ["java", "-jar", "app.jar"]