FROM openjdk:8-jdk-alpine
VOLUME /tmp
RUN mkdir -p /usr/shared-lib
RUN wget 'http://jdbc.postgresql.org/download/postgresql-42.2.4.jar' -O '/usr/shared-lib/jdbc-driver.jar'
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres?useUnicode=true&characterEncoding=utf8&useSSL=false
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=password
ARG JAR_FILE
EXPOSE 8080
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-cp" ,"/app.jar:/usr/shared-lib/*","org.springframework.boot.loader.JarLauncher"]
