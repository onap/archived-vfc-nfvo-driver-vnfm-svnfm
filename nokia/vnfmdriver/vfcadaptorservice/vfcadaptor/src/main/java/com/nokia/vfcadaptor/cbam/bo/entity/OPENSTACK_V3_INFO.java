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
package com.nokia.vfcadaptor.cbam.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OPENSTACK_V3_INFO {
	
	@JsonProperty("vimInfoType")
	private VimInfoType vimInfoType;
	@JsonProperty("interfaceInfo")
	private EndpointInfo interfaceInfo;
	@JsonProperty("id")
	private String id;
	@JsonProperty("accessInfo")
	private OpenStackAccessInfoV3 accessInfo;
	public VimInfoType getVimInfoType() {
		return vimInfoType;
	}
	public void setVimInfoType(VimInfoType vimInfoType) {
		this.vimInfoType = vimInfoType;
	}
	public EndpointInfo getInterfaceInfo() {
		return interfaceInfo;
	}
	public void setInterfaceInfo(EndpointInfo interfaceInfo) {
		this.interfaceInfo = interfaceInfo;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public OpenStackAccessInfoV3 getAccessInfo() {
		return accessInfo;
	}
	public void setAccessInfo(OpenStackAccessInfoV3 accessInfo) {
		this.accessInfo = accessInfo;
	}
	
	

}
