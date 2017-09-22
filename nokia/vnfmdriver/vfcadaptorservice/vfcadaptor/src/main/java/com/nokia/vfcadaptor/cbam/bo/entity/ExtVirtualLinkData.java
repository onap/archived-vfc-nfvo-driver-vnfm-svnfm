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

public class ExtVirtualLinkData {
	@JsonProperty("flavourId")
	private String resourceId;
	@JsonProperty("flavourId")
	private String vimId;
	@JsonProperty("flavourId")
	private String extVirtualLinkId;
	
	@JsonProperty("extVirtualLink")
	private List<VnfExtCpData> extCps=new ArrayList<VnfExtCpData>();

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
	}

	public String getExtVirtualLinkId() {
		return extVirtualLinkId;
	}

	public void setExtVirtualLinkId(String extVirtualLinkId) {
		this.extVirtualLinkId = extVirtualLinkId;
	}

	public List<VnfExtCpData> getExtCps() {
		return extCps;
	}

	public void setExtCps(List<VnfExtCpData> extCps) {
		this.extCps = extCps;
	}
	
   
}
