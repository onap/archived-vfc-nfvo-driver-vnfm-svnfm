package org.onap.vfc.nfvo.driver.vnfm.svnfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages= {"org.onap.vfc.nfvo.driver.vnfm.svnfm.example"})
@EnableAutoConfiguration
public class ExampleApplication extends SpringBootServletInitializer{

	 public static void main1(String[] args) {
	        SpringApplication.run(ExampleApplication.class, args);
	    }

}
