#!/bin/bash
add_user(){

	useradd  onap
		  chown onap:onap -R /service
}

set_up_tomcat(){

	# Set up tomcat
	wget -q https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.30/bin/apache-tomcat-8.5.30.tar.gz && \
		tar --strip-components=1 -xf apache-tomcat-8.5.30.tar.gz && \
		rm -f apache-tomcat-8.5.30.tar.gz && \
		rm -rf webapps && \
		mkdir -p webapps/ROOT
	  echo 'export CATALINA_OPTS="$CATALINA_OPTS -Xms64m -Xmx256m -XX:MaxPermSize=64m"' > /service/bin/setenv.sh

	# Set up microservice
	  wget -q -O nfvo-driver-vnfm-huawei.zip "https://nexus.onap.org/service/local/artifact/maven/redirect?r=snapshots&g=org.onap.vfc.nfvo.driver.vnfm.svnfm.huawei.vnfmadapter&a=hw-vnfmadapter-deployment&v=${PKG_VERSION}-SNAPSHOT&e=zip" && \
		unzip -q -o -B nfvo-driver-vnfm-huawei.zip && \
		rm -f nfvo-driver-vnfm-huawei.zip

	# Set permissions
	find . -type d -exec chmod o-w {} \;
	find . -name "*.sh" -exec chmod +x {} \;
	
	chown onap:onap -R /service
	chmod g+s /service
	setfacl -d --set u:onap:rwx /service
}


clean_sf_cache(){
															
	yum clean all
}

set_up_tomcat
wait
clean_sf_cache
