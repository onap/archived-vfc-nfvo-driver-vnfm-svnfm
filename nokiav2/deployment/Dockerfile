FROM centos:7
WORKDIR /service
ARG REPO
ARG MODE
COPY src/main/resources/docker-entrypoint.sh .
COPY src/main/resources/LICENSE ./ONAP_LICENSE
COPY src/main/resources/application.properties .
RUN sed -i 's/enabled=1/enabled=0/' /etc/yum/pluginconf.d/fastestmirror.conf
RUN sed -i 's|#baseurl=http://mirror.centos.org/centos|baseurl=http://mirrors.ocf.berkeley.edu/centos|' /etc/yum.repos.d/*.repo
RUN yum -y update && \
    yum -y install java-1.8.0-openjdk-headless wget && \
    yum -y install mc vim tcpdump net-tools nc strace telnet unzip && \
    yum clean all
COPY pom.xml pom.xml
RUN VERSION=`echo -e "setns x=http://maven.apache.org/POM/4.0.0 \n  xpath /x:project/x:version/text() "| xmllint --shell pom.xml | grep content | sed 's/.*content=//'| sed 's/-SNAPSHOT//'` ; echo "Version $VERSION mode=$MODE"; wget -q -O driver.war "https://nexus.onap.org/service/local/artifact/maven/redirect?r=${REPO}&g=org.onap.vfc.nfvo.driver.vnfm.svnfm.nokiav2&a=driverwar&v=${VERSION}${MODE}&e=war"
#Can be used for local builds instead of the previous line
#COPY driver.war .
RUN useradd  onap && chown onap:onap -R /service && chmod g+s /service && setfacl -d --set u:onap:rwx /service
USER onap
ENV JAVA_HOME /usr/lib/jvm/jre
EXPOSE 8089
ENTRYPOINT /service/docker-entrypoint.sh
