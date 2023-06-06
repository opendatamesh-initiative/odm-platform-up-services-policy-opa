# Stage 1
FROM maven:3-openjdk-11-slim as build

WORKDIR /workspace/app

COPY pom.xml .
COPY src src
COPY scripts/redoc-static-html-gen.sh scripts/redoc-static-html-gen.sh

RUN mvn clean install -DskipTests

# Stage 2
FROM openjdk:11-jre-slim

ARG SPRING_PROFILES_ACTIVE=docker
ARG JAVA_OPTS
ARG DATABASE_URL
ARG DATABASE_USERNAME
ARG DATABASE_PASSWORD
ARG FLYWAY_SCHEMA=flyway
ARG FLYWAY_SCRIPTS_DIR=postgres
ARG OPA_HOSTNAME=localhost
ARG OPA_PORT=8081
ARG SPRING_PORT=4242
ENV SPRING_PROFILES_ACTIVE ${SPRING_PROFILES_ACTIVE}
ENV JAVA_OPTS ${JAVA_OPTS}
ENV DATABASE_URL ${DATABASE_URL}
ENV DATABASE_USERNAME ${DATABASE_USERNAME}
ENV DATABASE_PASSWORD ${DATABASE_PASSWORD}
ENV FLYWAY_SCHEMA ${FLYWAY_SCHEMA}
ENV FLYWAY_SCRIPTS_DIR ${FLYWAY_SCRIPTS_DIR}
ENV OPA_HOSTNAME ${OPA_HOSTNAME}
ENV OPA_LOCAL_PORT ${OPA_PORT}
ENV SPRING_LOCAL_PORT ${SPRING_PORT}

COPY --from=build  /workspace/app/target/policyservice-opa-*.jar /app/

RUN ln -s -f /usr/share/zoneinfo/Europe/Rome /etc/localtime

CMD java $JAVA_OPTS -jar /app/policyservice-opa-*.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE

EXPOSE $SPRING_PORT