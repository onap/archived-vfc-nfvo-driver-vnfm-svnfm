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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PortResource {
	
	@JsonProperty("vimid")
	private String vimid;
	@JsonProperty("resourceid")
	private String resourceid;
	@JsonProperty("resourceName")
	private String resourceName;
	@JsonProperty("tenant")
	private String tenant;
	@JsonProperty("ipAddress")
	private String ipAddress;
	@JsonProperty("macAddress")
	private String macAddress;
	@JsonProperty("instId")
	private String instId;
	
	public String getVimid() {
		return vimid;
	}
	public void setVimid(String vimid) {
		this.vimid = vimid;
	}
	public String getResourceid() {
		return resourceid;
	}
	public void setResourceid(String resourceid) {
		this.resourceid = resourceid;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getInstId() {
		return instId;
	}
	public void setInstId(String instId) {
		this.instId = instId;
	}
	
	
}
