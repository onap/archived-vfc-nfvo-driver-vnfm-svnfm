package org.onap.vfc.nfvo.driver.vnfm.svnfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
//@ComponentScan(basePackages= {"org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.controller"})
@ComponentScan
public class VfcadaptorApplication {

	public static void main(String[] args) {
		SpringApplication.run(VfcadaptorApplication.class, args);
	}
}
