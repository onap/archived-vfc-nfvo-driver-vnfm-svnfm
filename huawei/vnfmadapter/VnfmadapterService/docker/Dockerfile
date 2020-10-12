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

# Install commonly needed tools
RUN apk --no-cache add curl netcat-openbsd nss apache2-utils java-cacerts

# Create symlink for default Java truststore
RUN set -eux; \
    rm -rf "$JAVA_HOME/lib/security/cacerts"; \
    ln -sT /etc/ssl/certs/java/cacerts "$JAVA_HOME/lib/security/cacerts"