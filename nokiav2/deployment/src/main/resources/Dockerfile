FROM centos:7
WORKDIR /service
ARG VERSION
COPY docker-entrypoint.sh .
COPY LICENSE ./ONAP_LICENSE
COPY application.properties .
RUN sed -i 's/enabled=1/enabled=0/' /etc/yum/pluginconf.d/fastestmirror.conf
RUN sed -i 's|#baseurl=http://mirror.centos.org/centos|baseurl=http://mirrors.ocf.berkeley.edu/centos|' /etc/yum.repos.d/*.repo
RUN yum -y update && \
    yum -y install java-1.8.0-openjdk-headless wget && \
    yum -y install mc vim tcpdump net-tools nc strace telnet unzip xmlstarlet && \
    yum clean all
#RUN wget -q -O driver.war "https://nexus.onap.org/service/local/artifact/maven/redirect?r=snapshots&g=org.onap.vfc.nfvo.driver.vnfm.svnfm.nokiav2&a=driverwar&v=${VERSION}&e=war"
#Can be used for local builds
COPY driver.war .
ENV JAVA_HOME /usr/lib/jvm/jre
EXPOSE 8089
ENTRYPOINT /service/docker-entrypoint.sh
