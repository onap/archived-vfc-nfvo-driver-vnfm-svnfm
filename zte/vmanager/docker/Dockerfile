FROM ubuntu:14.04

ARG HTTP_PROXY=${HTTP_PROXY}
ARG HTTPS_PROXY=${HTTPS_PROXY}

ENV http_proxy $HTTP_PROXY
ENV https_proxy $HTTPS_PROXY

RUN apt-get update && \
    apt-get install -y python-virtualenv && \
    apt-get install -y python-setuptools && \
    apt-get install -y python-dev && \
    apt-get install -y python-pip && \
    apt-get install -y gcc && \
    apt-get install -y wget && \
    apt-get install -y unzip && \
    apt-get install -y curl && \
    apt-get install -y build-essential && \
    apt-get install -y libssl-dev && \
    apt-get install -y libffi-dev

ADD . /service
WORKDIR /service

# get binary zip from nexus - vfc-nfvo-driver-vnfm-svnfm-zte-vmanager
RUN wget -q -O vfc-nfvo-driver-vnfm-svnfm-zte-vmanager.zip 'https://nexus.onap.org/service/local/artifact/maven/redirect?r=snapshots&g=org.onap.vfc.nfvo.driver.vnfm.svnfm.zte.vmanager&a=vfc-nfvo-driver-vnfm-svnfm-zte-vmanager&v=1.1.0-SNAPSHOT&e=zip' && \
    unzip vfc-nfvo-driver-vnfm-svnfm-zte-vmanager.zip && \
    rm -rf vfc-nfvo-driver-vnfm-svnfm-zte-vmanager.zip

WORKDIR /service/vfc/nfvo/driver/vnfm/svnfm/zte/vmanager
RUN pip install -r requirements.txt

EXPOSE 8410

WORKDIR /service
ENTRYPOINT vfc/nfvo/driver/vnfm/svnfm/zte/vmanager/docker/docker-entrypoint.sh
