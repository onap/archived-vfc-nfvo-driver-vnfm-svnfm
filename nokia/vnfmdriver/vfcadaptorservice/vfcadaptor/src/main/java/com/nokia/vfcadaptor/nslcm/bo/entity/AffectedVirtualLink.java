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
package com.nokia.vfcadaptor.nslcm.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokia.vfcadaptor.constant.CommonEnum;

public class AffectedVirtualLink {
	@JsonProperty("vlInstanceId")
	private String vlInstanceId;
	@JsonProperty("vldid")
	private String vldid;
	@JsonProperty("changeType")
	private CommonEnum.changeType changeType;
	
	@JsonProperty("networkResource")
	private ResourceHandle networkResource;

	public String getVlInstanceId() {
		return vlInstanceId;
	}

	public void setVlInstanceId(String vlInstanceId) {
		this.vlInstanceId = vlInstanceId;
	}

	public String getVldid() {
		return vldid;
	}

	public void setVldid(String vldid) {
		this.vldid = vldid;
	}

	public CommonEnum.changeType getChangeType() {
		return changeType;
	}

	public void setChangeType(CommonEnum.changeType changeType) {
		this.changeType = changeType;
	}

	public ResourceHandle getNetworkResource() {
		return networkResource;
	}

	public void setNetworkResource(ResourceHandle networkResource) {
		this.networkResource = networkResource;
	}
	
	
	
	

}
