FROM openjdk
EXPOSE 80
COPY ./target/wopr-0.1.0.jar /app/wopr.jar
ENTRYPOINT ["java","-jar", "/app/wopr.jar", "--server.port=80"]