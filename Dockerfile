FROM gradle:6.8.0-jdk15 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build

FROM adoptopenjdk:15-jdk-hotspot

COPY build/libs/bitimage-all.jar bitimage-all.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "bitimage-all.jar"]