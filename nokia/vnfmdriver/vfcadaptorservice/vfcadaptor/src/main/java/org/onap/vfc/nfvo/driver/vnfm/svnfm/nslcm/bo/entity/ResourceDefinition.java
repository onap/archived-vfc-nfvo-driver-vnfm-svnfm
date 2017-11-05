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

public class ResourceDefinition {
	
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;
	@JsonProperty("addResource")
	private List<AddResource> addResource;
	@JsonProperty("vimId")
	private String vimId;
	@JsonProperty("additionalParam")
	private AdditionalParam additionalParam;

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public List<AddResource> getAddResource() {
		return addResource;
	}

	public void setAddResource(List<AddResource> addResource) {
		this.addResource = addResource;
	}

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
	}

	public AdditionalParam getAdditionalParam() {
		return additionalParam;
	}

	public void setAdditionalParam(AdditionalParam additionalParam) {
		this.additionalParam = additionalParam;
	}
	
	/*@JsonProperty("type")
	private CommonEnum.type type;
	
	@JsonProperty("resourceDefinitionId")
	private String resourceDefinitionId;
	
	@JsonProperty("vdu")
	private String vdu;*/

	/*public CommonEnum.type getType() {
		return type;
	}

	public void setType(CommonEnum.type type) {
		this.type = type;
	}

	public String getResourceDefinitionId() {
		return resourceDefinitionId;
	}

	public void setResourceDefinitionId(String resourceDefinitionId) {
		this.resourceDefinitionId = resourceDefinitionId;
	}

	public String getVdu() {
		return vdu;
	}

	public void setVdu(String vdu) {
		this.vdu = vdu;
	}
	*/
	
	

}
