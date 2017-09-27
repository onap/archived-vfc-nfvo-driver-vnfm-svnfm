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

public class InterfaceInfo {
   
	@JsonProperty("vimType")
	private String vimType;
	
	@JsonProperty("apiVersion")
	private String apiVersion;
	
	@JsonProperty("protocolType")
	private String protocolType;

	public String getVimType() {
		return vimType;
	}

	public void setVimType(String vimType) {
		this.vimType = vimType;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}
	
	
	
	
	
}
