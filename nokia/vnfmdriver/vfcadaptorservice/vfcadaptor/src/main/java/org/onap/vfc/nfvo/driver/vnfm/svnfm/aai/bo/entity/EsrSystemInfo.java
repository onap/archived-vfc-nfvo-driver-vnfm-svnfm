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
import com.google.gson.annotations.SerializedName;

public class EsrSystemInfo {
	@JsonProperty("esr-system-info-id")
	@SerializedName("esr-system-info-id")
	private String esrSystemId;
	
	@JsonProperty("system-name")
	@SerializedName("system-name")
	private String esrSystemName;
	
	@JsonProperty("type")
	@SerializedName("type")
	private String type;
	
	@JsonProperty("vendor")
	@SerializedName("vendor")
	private String vendor;
	
	@JsonProperty("version")
	@SerializedName("version")
	private String version;
	
	@JsonProperty("service-url")
	@SerializedName("service-url")
	private String serviceUrl;
	
	@JsonProperty("user-name")
	@SerializedName("user-name")
	private String userName;
	
	@JsonProperty("password")
	@SerializedName("password")
	private String password;
	
	@JsonProperty("system-type")
	@SerializedName("system-type")
	private String systemType;
	
	@JsonProperty("resource-version")
	@SerializedName("resource-version")
	private String resourceVersion;
	
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

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}
}
