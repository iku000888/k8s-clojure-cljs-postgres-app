FROM amazoncoretto:11.0.9
WORKDIR /app
COPY ./target/*-standalone.jar ./app.jar
CMD ["java", "-jar", "app.jar"]