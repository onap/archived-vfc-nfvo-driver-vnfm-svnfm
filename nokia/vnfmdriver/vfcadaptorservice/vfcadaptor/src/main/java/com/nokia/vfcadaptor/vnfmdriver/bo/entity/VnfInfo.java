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
package com.nokia.vfcadaptor.vnfmdriver.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokia.vfcadaptor.constant.CommonEnum;

public class VnfInfo {
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;
	
	@JsonProperty("vnfInstanceName")
	private String vnfInstanceName;
	
	@JsonProperty("vnfInstanceDescription")
	private String vnfInstanceDescription;
	
	@JsonProperty("vnfdId")
	private String vnfdId;
	
	@JsonProperty("vnfPackageId")
	private String vnfPackageId;
	
	@JsonProperty("version")
	private String version;
	
	@JsonProperty("vnfProvider")
	private String vnfProvider;
	
	@JsonProperty("vnfType")
	private String vnfType;
	
	@JsonProperty("vnfStatus")
	private CommonEnum.InstantiationState vnfStatus;

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public String getVnfInstanceName() {
		return vnfInstanceName;
	}

	public void setVnfInstanceName(String vnfInstanceName) {
		this.vnfInstanceName = vnfInstanceName;
	}

	public String getVnfInstanceDescription() {
		return vnfInstanceDescription;
	}

	public void setVnfInstanceDescription(String vnfInstanceDescription) {
		this.vnfInstanceDescription = vnfInstanceDescription;
	}

	public String getVnfdId() {
		return vnfdId;
	}

	public void setVnfdId(String vnfdId) {
		this.vnfdId = vnfdId;
	}

	public String getVnfPackageId() {
		return vnfPackageId;
	}

	public void setVnfPackageId(String vnfPackageId) {
		this.vnfPackageId = vnfPackageId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVnfProvider() {
		return vnfProvider;
	}

	public void setVnfProvider(String vnfProvider) {
		this.vnfProvider = vnfProvider;
	}

	public String getVnfType() {
		return vnfType;
	}

	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}

	public CommonEnum.InstantiationState getVnfStatus() {
		return vnfStatus;
	}

	public void setVnfStatus(CommonEnum.InstantiationState vnfStatus) {
		this.vnfStatus = vnfStatus;
	}

	
	
	

}
