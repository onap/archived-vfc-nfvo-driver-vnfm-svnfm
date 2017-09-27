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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtManagedVirtualLinkData {
	@JsonProperty("extManagedVirtualLinkId")
	private String extManagedVirtualLinkId;
	
	@JsonProperty("resourceId")
	private String resourceId;
	
	@JsonProperty("virtualLinkDescId")
	private String virtualLinkDescId;
	
	@JsonProperty("vimId")
	private String vimId;

	public String getExtManagedVirtualLinkId() {
		return extManagedVirtualLinkId;
	}

	public void setExtManagedVirtualLinkId(String extManagedVirtualLinkId) {
		this.extManagedVirtualLinkId = extManagedVirtualLinkId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getVirtualLinkDescId() {
		return virtualLinkDescId;
	}

	public void setVirtualLinkDescId(String virtualLinkDescId) {
		this.virtualLinkDescId = virtualLinkDescId;
	}

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
	}
	
	

}
