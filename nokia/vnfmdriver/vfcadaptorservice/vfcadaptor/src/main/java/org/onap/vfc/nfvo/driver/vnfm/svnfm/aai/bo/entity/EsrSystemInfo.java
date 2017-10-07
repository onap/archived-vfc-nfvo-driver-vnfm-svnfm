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

import com.google.gson.annotations.SerializedName;

public class EsrSystemInfo {
	@SerializedName("esr-system-info-id")
	private String esrSystemId;
	
	@SerializedName("system-name")
	private String esrSystemName;
	
	@SerializedName("type")
	private String type;
	
	@SerializedName("vendor")
	private String vendor;
	
	@SerializedName("version")
	private String version;
	
	@SerializedName("service-url")
	private String serviceUrl;
	
	@SerializedName("user-name")
	private String userName;
	
	@SerializedName("password")
	private String password;
	
	@SerializedName("protocal")
	private String protocal;
	
	@SerializedName("ssl-cacert")
	private String sslCacert;
	
	@SerializedName("ssl-insecure")
	private String sslInsecure;
	
	@SerializedName("ip-address")
	private String ip;
	
	@SerializedName("port")
	private String port;
	
	@SerializedName("cloud-domain")
	private String cloudDomain;
	
	@SerializedName("default-tenant")
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
