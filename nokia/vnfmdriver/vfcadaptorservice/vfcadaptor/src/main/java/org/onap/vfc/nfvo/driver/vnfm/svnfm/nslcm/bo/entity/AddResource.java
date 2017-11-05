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

public class AddResource {
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("resourceTemplate")
	private ResourceTemplate resourceTemplate;
	
	@JsonProperty("resourceDefinitionId")
	private int  resourceDefinitionId;
	
	@JsonProperty("vdu")
	private String vdu;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ResourceTemplate getResourceTemplate() {
		return resourceTemplate;
	}
	public void setResourceTemplate(ResourceTemplate resourceTemplate) {
		this.resourceTemplate = resourceTemplate;
	}
	public int getResourceDefinitionId() {
		return resourceDefinitionId;
	}
	public void setResourceDefinitionId(int resourceDefinitionId) {
		this.resourceDefinitionId = resourceDefinitionId;
	}
	public String getVdu() {
		return vdu;
	}
	public void setVdu(String vdu) {
		this.vdu = vdu;
	}
	
	
	
	
	
}
