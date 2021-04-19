#VFC-1837/VFC-1842 need to validate
FROM python:3.9-alpine

ARG PKG_VERSION
ENV JAVA_HOME /usr/lib/jvm/jre
ENV CATALINA_HOME /service
ADD . /service
WORKDIR /service
RUN bash docker-env-config.sh
VOLUME /var/lib/mysql


EXPOSE 8482
USER onap
ENTRYPOINT /service/docker-entrypoint.sh
