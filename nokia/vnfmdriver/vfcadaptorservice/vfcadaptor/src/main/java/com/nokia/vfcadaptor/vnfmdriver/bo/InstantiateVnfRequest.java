/*
* Copyright 2016-2017 Nokia Corporation
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
package com.nokia.vfcadaptor.vnfmdriver.bo;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.nokia.vfcadaptor.vnfmdriver.bo.entity.AdditionalParam;
import com.nokia.vfcadaptor.vnfmdriver.bo.entity.ExtVirtualLinkData;

//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,property = "@id")
//  -- zhouyufei added property id cause problem  --  415 Unsupported Media Type (UTF-8 is not supported)
public class InstantiateVnfRequest {
	@JsonProperty("vnfInstanceName")
	private String vnfInstanceName;
	
	@JsonProperty("vnfPackageId")
	private String vnfPackageId;
	
	@JsonProperty("vnfDescriptorId")
	private String vnfDescriptorId;
	
	@JsonProperty("flavourId")
	private String flavourId;
	
	@JsonProperty("vnfInstanceDescription")
	private String vnfInstanceDescription;
	
	@JsonProperty("extVirtualLink")
//	@JsonBackReference  -- zhouyufei deprecated, and mustn't initiate by new ArrayList;
//	private List<ExtVirtualLinkData> extVirtualLink = new ArrayList<ExtVirtualLinkData>();
	private List<ExtVirtualLinkData> extVirtualLink;

	@JsonProperty("additionalParam")
	private AdditionalParam additionalParam;
	
	

	public String getVnfInstanceName() {
		return vnfInstanceName;
	}

	public void setVnfInstanceName(String vnfInstanceName) {
		this.vnfInstanceName = vnfInstanceName;
	}

	public String getVnfPackageId() {
		return vnfPackageId;
	}

	public void setVnfPackageId(String vnfPackageId) {
		this.vnfPackageId = vnfPackageId;
	}

	public String getVnfDescriptorId() {
		return vnfDescriptorId;
	}

	public void setVnfDescriptorId(String vnfDescriptorId) {
		this.vnfDescriptorId = vnfDescriptorId;
	}

	public String getFlavourId() {
		return flavourId;
	}

	public void setFlavourId(String flavourId) {
		this.flavourId = flavourId;
	}

	public String getVnfInstanceDescription() {
		return vnfInstanceDescription;
	}

	public void setVnfInstanceDescription(String vnfInstanceDescription) {
		this.vnfInstanceDescription = vnfInstanceDescription;
	}

	public AdditionalParam getAdditionalParam() {
		return additionalParam;
	}

	public void setAdditionalParam(AdditionalParam additionalParam) {
		this.additionalParam = additionalParam;
	}

	public List<ExtVirtualLinkData> getExtVirtualLink() {
		return extVirtualLink;
	}

	public void setExtVirtualLink(List<ExtVirtualLinkData> extVirtualLink) {
		this.extVirtualLink = extVirtualLink;
	}

}


