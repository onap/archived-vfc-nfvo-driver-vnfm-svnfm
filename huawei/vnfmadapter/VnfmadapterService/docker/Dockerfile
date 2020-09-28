FROM centos:7

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
