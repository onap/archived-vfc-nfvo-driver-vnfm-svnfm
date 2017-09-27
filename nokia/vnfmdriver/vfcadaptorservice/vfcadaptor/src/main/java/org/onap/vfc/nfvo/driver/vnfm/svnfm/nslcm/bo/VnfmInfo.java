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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VnfmInfo {
	@JsonProperty("vnfmId")
	private String vnfmId;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("url")
	private String url;
	
	@JsonProperty("userName")
	private String userName;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("vimId")
	private String vimId;
	
	@JsonProperty("vendor")
	private String vendor;
	
	@JsonProperty("version")
	private String version;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("certificateUrl")
	private String certificateUrl;
	
	@JsonProperty("createTime")
	private String createTime;

	public String getVnfmId() {
		return vnfmId;
	}

	public void setVnfmId(String vnfmId) {
		this.vnfmId = vnfmId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCertificateUrl() {
		return certificateUrl;
	}

	public void setCertificateUrl(String certificateUrl) {
		this.certificateUrl = certificateUrl;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
