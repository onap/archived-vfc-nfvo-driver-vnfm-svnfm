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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AffectedCp {
	
	@JsonProperty("virtualLinkInstanceId")
    private String virtualLinkInstanceId;
    @JsonProperty("cpinstanceid")
    private String cpinstanceid;
    @JsonProperty("cpdid")
    private String cpdid;
    @JsonProperty("ownerType")
    private String  ownerType;
    @JsonProperty("ownerId")
    private String ownerId;
    @JsonProperty("changeType")
	private CommonEnum.changeType changeType;
    @JsonProperty("portResource")
    private PortResource portResource;
    
	public String getVirtualLinkInstanceId() {
		return virtualLinkInstanceId;
	}
	public void setVirtualLinkInstanceId(String virtualLinkInstanceId) {
		this.virtualLinkInstanceId = virtualLinkInstanceId;
	}
	public String getCpinstanceid() {
		return cpinstanceid;
	}
	public void setCpinstanceid(String cpinstanceid) {
		this.cpinstanceid = cpinstanceid;
	}
	public String getCpdid() {
		return cpdid;
	}
	public void setCpdid(String cpdid) {
		this.cpdid = cpdid;
	}
	public String getOwnerType() {
		return ownerType;
	}
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public CommonEnum.changeType getChangeType() {
		return changeType;
	}
	public void setChangeType(CommonEnum.changeType changeType) {
		this.changeType = changeType;
	}
	public PortResource getPortResource() {
		return portResource;
	}
	public void setPortResource(PortResource portResource) {
		this.portResource = portResource;
	}
    
    
    
    

}
