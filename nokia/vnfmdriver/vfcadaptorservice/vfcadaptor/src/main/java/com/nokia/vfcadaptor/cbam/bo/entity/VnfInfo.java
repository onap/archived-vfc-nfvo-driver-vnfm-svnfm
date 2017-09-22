
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

package com.nokia.vfcadaptor.cbam.bo.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokia.vfcadaptor.constant.CommonEnum;

public class VnfInfo {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("vnfdId")
	private String vnfdId;
	
	@JsonProperty("vnfProvider")
	private String vnfProvider;
	
	@JsonProperty("vnfProductName")
	private String vnfProductName;
	
	@JsonProperty("vnfSoftwareVersion")
	private String vnfSoftwareVersion;
	
	@JsonProperty("vnfdVersion")
	private String vnfdVersion;
	
	@JsonProperty("onboardedVnfPkgInfoId")
	private String onboardedVnfPkgInfoId;
	
	@JsonProperty("instantiationState")
	private CommonEnum.InstantiationState instantiationState;
	
	@JsonProperty("operationExecution")
	private List<OperationExecution> operationExecution=new ArrayList<OperationExecution>();
	
	@JsonProperty("instantiatedVnfInfo")
	private InstantiatedVnfInfo instantiatedVnfInfo;
	
	@JsonProperty("vnfConfigurableProperties")
	private List<VnfProperty> vnfConfigurableProperties=new ArrayList<VnfProperty>();
	
	@JsonProperty("extensions")
	private List<VnfProperty> extensions=new ArrayList<VnfProperty>();
	
	@JsonProperty("metadata")
	private Object metadata;
	
	@JsonProperty("link")
	private _links link;//todo
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVnfdId() {
		return vnfdId;
	}

	public void setVnfdId(String vnfdId) {
		this.vnfdId = vnfdId;
	}

	public String getVnfProvider() {
		return vnfProvider;
	}

	public void setVnfProvider(String vnfProvider) {
		this.vnfProvider = vnfProvider;
	}

	public String getVnfProductName() {
		return vnfProductName;
	}

	public void setVnfProductName(String vnfProductName) {
		this.vnfProductName = vnfProductName;
	}

	public String getVnfSoftwareVersion() {
		return vnfSoftwareVersion;
	}

	public void setVnfSoftwareVersion(String vnfSoftwareVersion) {
		this.vnfSoftwareVersion = vnfSoftwareVersion;
	}

	public String getVnfdVersion() {
		return vnfdVersion;
	}

	public void setVnfdVersion(String vnfdVersion) {
		this.vnfdVersion = vnfdVersion;
	}

	public String getOnboardedVnfPkgInfoId() {
		return onboardedVnfPkgInfoId;
	}

	public void setOnboardedVnfPkgInfoId(String onboardedVnfPkgInfoId) {
		this.onboardedVnfPkgInfoId = onboardedVnfPkgInfoId;
	}



	public CommonEnum.InstantiationState getInstantiationState() {
		return instantiationState;
	}

	public void setInstantiationState(CommonEnum.InstantiationState instantiationState) {
		this.instantiationState = instantiationState;
	}

	public List<OperationExecution> getOperationExecution() {
		return operationExecution;
	}

	public void setOperationExecution(List<OperationExecution> operationExecution) {
		this.operationExecution = operationExecution;
	}

	public InstantiatedVnfInfo getInstantiatedVnfInfo() {
		return instantiatedVnfInfo;
	}

	public void setInstantiatedVnfInfo(InstantiatedVnfInfo instantiatedVnfInfo) {
		this.instantiatedVnfInfo = instantiatedVnfInfo;
	}

	public List<VnfProperty> getVnfConfigurableProperties() {
		return vnfConfigurableProperties;
	}

	public void setVnfConfigurableProperties(List<VnfProperty> vnfConfigurableProperties) {
		this.vnfConfigurableProperties = vnfConfigurableProperties;
	}

	public List<VnfProperty> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<VnfProperty> extensions) {
		this.extensions = extensions;
	}

	public Object getMetadata() {
		return metadata;
	}

	public void setMetadata(Object metadata) {
		this.metadata = metadata;
	}

	public _links getLink() {
		return link;
	}

	public void setLink(_links link) {
		this.link = link;
	}

	
}
