
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

package com.nokia.vfcadaptor.cbam.bo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokia.vfcadaptor.cbam.bo.entity.ExtManagedVirtualLinkData;
import com.nokia.vfcadaptor.cbam.bo.entity.ExtVirtualLinkData;
import com.nokia.vfcadaptor.cbam.bo.entity.VimInfo;

public class CBAMInstantiateVnfRequest {
	
	@JsonProperty("flavourId")
	private String flavourId;
	
	@JsonProperty("vims")
	private List<VimInfo> vims;
	
	@JsonProperty("extVirtualLinks")
	private List<ExtVirtualLinkData> extVirtualLinks;
	
	@JsonProperty("extManagedVirtualLinks")
	private List<ExtManagedVirtualLinkData> extManagedVirtualLinks;

	public String getFlavourId() {
		return flavourId;
	}

	public void setFlavourId(String flavourId) {
		this.flavourId = flavourId;
	}

	

	public List<VimInfo> getVims() {
		return vims;
	}

	public void setVims(List<VimInfo> vims) {
		this.vims = vims;
	}

	public List<ExtVirtualLinkData> getExtVirtualLinks() {
		return extVirtualLinks;
	}

	public void setExtVirtualLinks(List<ExtVirtualLinkData> extVirtualLinks) {
		this.extVirtualLinks = extVirtualLinks;
	}

	public List<ExtManagedVirtualLinkData> getExtManagedVirtualLinks() {
		return extManagedVirtualLinks;
	}

	public void setExtManagedVirtualLinks(List<ExtManagedVirtualLinkData> extManagedVirtualLinks) {
		this.extManagedVirtualLinks = extManagedVirtualLinks;
	}
	
	


	
	
	

}
