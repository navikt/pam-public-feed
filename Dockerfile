FROM ghcr.io/navikt/baseimages/temurin:21
COPY target/pam-public-feed-*-jar-with-dependencies.jar /app/app.jar
ENV JAVA_OPTS="-Xms256m -Xmx1024m"
EXPOSE 9021
