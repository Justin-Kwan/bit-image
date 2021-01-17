FROM gradle:6.8.0-jdk15 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon 

FROM adoptopenjdk:15-jdk-hotspot

# Copy fat jar from gradle build folder to current classpath
COPY --from=build /home/gradle/src/build/libs/bitimage-all.jar /app/bitimage-all.jar

EXPOSE 8080

# Run, passing executable jar to JVM
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/bitimage-all.jar"]