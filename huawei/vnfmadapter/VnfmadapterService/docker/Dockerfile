#VFC-1837/VFC-1842 need to validate
FROM python:3.9-alpine

ARG HTTP_PROXY=${HTTP_PROXY}
ARG HTTPS_PROXY=${HTTPS_PROXY}
ENV JAVA_HOME /usr/lib/jvm/jre
ENV CATALINA_HOME /service
ENV http_proxy $HTTP_PROXY
ENV https_proxy $HTTPS_PROXY
ENV pkg_verison=${PKG_VERSION}

ADD . /service
WORKDIR /service

RUN sh /service/docker-env-config.sh

EXPOSE 8482
USER onap
WORKDIR /service
ENTRYPOINT /docker/docker-entrypoint.sh
