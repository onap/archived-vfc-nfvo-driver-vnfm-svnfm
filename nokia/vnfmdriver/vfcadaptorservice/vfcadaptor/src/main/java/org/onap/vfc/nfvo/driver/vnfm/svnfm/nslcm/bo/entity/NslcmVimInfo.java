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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NslcmVimInfo {
	
	@JsonProperty("vimInfoId")
	private String vimInfoId;
	
	@JsonProperty("vimId")
	private String vimId;
	@JsonProperty("interfaceInfo")
	private List<InterfaceInfo> interfaceInfo;
	@JsonProperty("accessInfo")
	private List<AccessInfo> accessInfo;
	
	@JsonProperty("interfaceEndpoint")
	private String interfaceEndpoint;

	public String getVimInfoId() {
		return vimInfoId;
	}

	public void setVimInfoId(String vimInfoId) {
		this.vimInfoId = vimInfoId;
	}

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
	}
     
	
	
	
	public List<InterfaceInfo> getInterfaceInfo() {
		return interfaceInfo;
	}

	public void setInterfaceInfo(List<InterfaceInfo> interfaceInfo) {
		this.interfaceInfo = interfaceInfo;
	}

	public List<AccessInfo> getAccessInfo() {
		return accessInfo;
	}

	public void setAccessInfo(List<AccessInfo> accessInfo) {
		this.accessInfo = accessInfo;
	}

	public String getInterfaceEndpoint() {
		return interfaceEndpoint;
	}

	public void setInterfaceEndpoint(String interfaceEndpoint) {
		this.interfaceEndpoint = interfaceEndpoint;
	}
	
	
	
	
	
	

}
