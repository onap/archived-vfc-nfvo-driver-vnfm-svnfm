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

import com.fasterxml.jackson.annotation.JsonProperty;

public class VimSoftwareImage {
	@JsonProperty("vimId")
	private String vimId;
	@JsonProperty("vnfdSoftwareImageId")
	private String vnfdSoftwareImageId;
	@JsonProperty("resourceId")
	private String resourceId;
	public String getVimId() {
		return vimId;
	}
	public void setVimId(String vimId) {
		this.vimId = vimId;
	}
	public String getVnfdSoftwareImageId() {
		return vnfdSoftwareImageId;
	}
	public void setVnfdSoftwareImageId(String vnfdSoftwareImageId) {
		this.vnfdSoftwareImageId = vnfdSoftwareImageId;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	

}
