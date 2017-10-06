/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EsrSystemInfo {
	@JsonProperty("esr-system-info-id")
	private String esrSystemId;
	
	@JsonProperty("system-name")
	private String esrSystemName;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("vendor")
	private String vendor;
	
	@JsonProperty("version")
	private String version;
	
	@JsonProperty("service-url")
	private String serviceUrl;
	
	@JsonProperty("user-name")
	private String userName;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("protocal")
	private String protocal;
	
	@JsonProperty("ssl-cacert")
	private String sslCacert;
	
	@JsonProperty("ssl-insecure")
	private String sslInsecure;
	
	@JsonProperty("ip-address")
	private String ip;
	
	@JsonProperty("port")
	private String port;
	
	@JsonProperty("cloud-domain")
	private String cloudDomain;
	
	@JsonProperty("default-tenant")
	private String defaultTenant;

	public String getEsrSystemId() {
		return esrSystemId;
	}

	public void setEsrSystemId(String esrSystemId) {
		this.esrSystemId = esrSystemId;
	}

	public String getEsrSystemName() {
		return esrSystemName;
	}

	public void setEsrSystemName(String esrSystemName) {
		this.esrSystemName = esrSystemName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProtocal() {
		return protocal;
	}

	public void setProtocal(String protocal) {
		this.protocal = protocal;
	}

	public String getSslCacert() {
		return sslCacert;
	}

	public void setSslCacert(String sslCacert) {
		this.sslCacert = sslCacert;
	}

	public String getSslInsecure() {
		return sslInsecure;
	}

	public void setSslInsecure(String sslInsecure) {
		this.sslInsecure = sslInsecure;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getCloudDomain() {
		return cloudDomain;
	}

	public void setCloudDomain(String cloudDomain) {
		this.cloudDomain = cloudDomain;
	}

	public String getDefaultTenant() {
		return defaultTenant;
	}

	public void setDefaultTenant(String defaultTenant) {
		this.defaultTenant = defaultTenant;
	}
	
}
