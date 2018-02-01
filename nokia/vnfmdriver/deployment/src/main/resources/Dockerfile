FROM centos:7
WORKDIR /service
COPY docker-entrypoint.sh .
COPY LICENSE ./ONAP_LICENSE
COPY application.properties .
COPY driver.war .

RUN sed -i 's/enabled=1/enabled=0/' /etc/yum/pluginconf.d/fastestmirror.conf
RUN sed -i 's|#baseurl=http://mirror.centos.org/centos|baseurl=http://mirrors.ocf.berkeley.edu/centos|' /etc/yum.repos.d/*.repo
RUN yum -y update
RUN yum -y install java-1.8.0-openjdk-headless
# Debugging tools withing the container
RUN yum -y install mc vim tcpdump wget net-tools nc strace telnet unzip
#RUN sed -i 's|#networkaddress.cache.ttl=-1|networkaddress.cache.ttl=10|' /usr/lib/jvm/jre/lib/security/java.security
ENV JAVA_HOME /usr/lib/jvm/jre
RUN yum clean all
EXPOSE 8089
ENTRYPOINT /service/docker-entrypoint.sh
