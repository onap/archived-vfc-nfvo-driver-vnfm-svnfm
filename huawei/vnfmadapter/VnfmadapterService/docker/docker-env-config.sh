#!/bin/bash

install_sf(){

        sed -i 's/enabled=1/enabled=0/' /etc/yum/pluginconf.d/fastestmirror.conf
        sed -i 's|#baseurl=http://mirror.centos.org/centos|baseurl=http://mirrors.ocf.berkeley.edu/centos|' /etc/yum.repos.d/*.repo
        yum -y update

        yum -y install wget unzip socat java-1.8.0-openjdk-headless
        sed -i 's|#networkaddress.cache.ttl=-1|networkaddress.cache.ttl=10|' /usr/lib/jvm/jre/lib/security/java.security

        # Set up mysql
        wget -q http://repo.mysql.com/mysql-community-release-el7-5.noarch.rpm && \
                rpm -ivh mysql-community-release-el7-5.noarch.rpm && \
                rm -f mysql-community-release-el7-5.noarch.rpm
        yum -y update
        yum -y install mysql-server
        mysql_install_db --user=mysql --datadir=/var/lib/mysql

        # Set up tomcat
        wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.30/bin/apache-tomcat-8.5.30.tar.gz && \
                tar --strip-components=1 -xf apache-tomcat-8.5.30.tar.gz && \
                rm -f apache-tomcat-8.5.30.tar.gz && \
                rm -rf webapps && \
                mkdir -p webapps/ROOT
        echo 'export CATALINA_OPTS="$CATALINA_OPTS -Xms64m -Xmx256m -XX:MaxPermSize=64m"' > /service/bin/setenv.sh

        # Set up microservice
        wget -q -O nfvo-driver-vnfm-huawei.zip "https://nexus.onap.org/service/local/artifact/maven/redirect?r=snapshots&g=org.onap.vfc.nfvo.driver.vnfm.svnfm.hu
awei.vnfmadapter&a=hw-vnfmadapter-deployment&v=${PKG_VERSION}-SNAPSHOT&e=zip" && \
                unzip -q -o -B nfvo-driver-vnfm-huawei.zip && \
                rm -f nfvo-driver-vnfm-huawei.zip

        # Set permissions
        find . -type d -exec chmod o-w {} \;
        find . -name "*.sh" -exec chmod +x {} \;
}

add_user(){

        useradd onap
        yum -y install sudo
        chmod u+x /etc/sudoers
        sed -i '/Same thing without a password/a\onap    ALL=(ALL:ALL) NOPASSWD:ALL' /etc/sudoers
        chmod u-x /etc/sudoers
        chown onap:onap -R /service
}

clean_sf_cache(){

        yum clean all
}

install_sf
wait
add_user
clean_sf_cache
