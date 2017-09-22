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
package com.nokia.vfcadaptor.nslcm.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokia.vfcadaptor.constant.CommonEnum;

public class VnfPackageInfo {
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("provider")
	private String provider;
	
	@JsonProperty("version")
	private String version;
	
	@JsonProperty("onBoardState")
	private String onBoardState;
	
	@JsonProperty("deletionPending")
	private CommonEnum.Deletionpending deletionPending;
	
	@JsonProperty("downloadUri")
	private String downloadUri;
	
	@JsonProperty("vnfdId")
	private String vnfdId;
	
	@JsonProperty("vnfdProvider")
	private String vnfdProvider;
	
	@JsonProperty("vnfdVersion")
	private String vnfdVersion;
	
	@JsonProperty("vnfVersion")
	private String vnfVersion;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOnBoardState() {
		return onBoardState;
	}

	public void setOnBoardState(String onBoardState) {
		this.onBoardState = onBoardState;
	}

	public CommonEnum.Deletionpending getDeletionPending() {
		return deletionPending;
	}

	public void setDeletionPending(CommonEnum.Deletionpending deletionPending) {
		this.deletionPending = deletionPending;
	}

	public String getDownloadUri() {
		return downloadUri;
	}

	public void setDownloadUri(String downloadUri) {
		this.downloadUri = downloadUri;
	}

	public String getVnfdId() {
		return vnfdId;
	}

	public void setVnfdId(String vnfdId) {
		this.vnfdId = vnfdId;
	}

	public String getVnfdProvider() {
		return vnfdProvider;
	}

	public void setVnfdProvider(String vnfdProvider) {
		this.vnfdProvider = vnfdProvider;
	}

	public String getVnfdVersion() {
		return vnfdVersion;
	}

	public void setVnfdVersion(String vnfdVersion) {
		this.vnfdVersion = vnfdVersion;
	}

	public String getVnfVersion() {
		return vnfVersion;
	}

	public void setVnfVersion(String vnfVersion) {
		this.vnfVersion = vnfVersion;
	}
	
	
	
	
	
	
	
	

}
