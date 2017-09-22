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
package com.nokia.vfcadaptor.nslcm.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AffectedVirtualStorage {
	@JsonProperty("vsInstanceId")
   private String vsInstanceId;
	@JsonProperty("vsdId")
   private String vsdId;
	
	@JsonProperty("changeType")
   private String changeType;
	@JsonProperty("storageResource")
   private String storageResource;
	public String getVsInstanceId() {
		return vsInstanceId;
	}
	public void setVsInstanceId(String vsInstanceId) {
		this.vsInstanceId = vsInstanceId;
	}
	public String getVsdId() {
		return vsdId;
	}
	public void setVsdId(String vsdId) {
		this.vsdId = vsdId;
	}
	public String getChangeType() {
		return changeType;
	}
	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}
	public String getStorageResource() {
		return storageResource;
	}
	public void setStorageResource(String storageResource) {
		this.storageResource = storageResource;
	}
   
   
}
