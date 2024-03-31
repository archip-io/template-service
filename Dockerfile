FROM amazoncorretto:17-alpine as jar-deps

WORKDIR /opt/deps
COPY target/*.jar app.jar

RUN unzip app.jar -d tmp &&  \
    jdeps  \
      --print-module-deps \
      --ignore-missing-deps \
      --recursive \
      --multi-release 17 \
      --class-path="./tmp/BOOT-INF/lib/*" \
      --module-path="./tmp/BOOT-INF/lib/*" \
      app.jar > modules.txt


FROM amazoncorretto:17-alpine AS correto-jre

ARG DEPS_PATH=/opt/deps

COPY --from=jar-deps $DEPS_PATH/modules.txt $DEPS_PATH/modules.txt

RUN apk add --no-cache binutils
RUN jlink \
    --verbose \
    --add-modules "$(cat $DEPS_PATH/modules.txt),jdk.crypto.ec,jdk.crypto.cryptoki" \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /opt/jre

FROM alpine:latest

ARG JRE_PATH=/opt/jre
ENV JAVA_HOME=$JRE_PATH
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /opt/workspace
COPY --from=correto-jre $JRE_PATH $JAVA_HOME
COPY target/*.jar template-service.jar

RUN addgroup -S admin && adduser -S admin -G admin
USER admin

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "template-service.jar"]