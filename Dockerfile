FROM navikt/java:11
COPY target/pam-public-feed-*-jar-with-dependencies.jar /app/app.jar
EXPOSE 9021
