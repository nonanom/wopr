FROM openjdk
EXPOSE 8080
COPY ./target/wopr-0.1.0.jar /app.jar
ENTRYPOINT ["java","-jar", "/app.jar", ""]
