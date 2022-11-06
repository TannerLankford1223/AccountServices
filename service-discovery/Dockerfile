FROM bellsoft/liberica-openjdk-alpine:17
COPY ./target/service-discovery-0.0.1-SNAPSHOT.jar /usr/src/eureka/
WORKDIR /usr/src/eureka
CMD ["java", "-jar", "service-discovery-0.0.1-SNAPSHOT.jar"]