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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.catalog.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageInfo {
	
	@JsonProperty("index")
	private String index;
	
	@JsonProperty("fileName")
	private String fileName;
	
	@JsonProperty("imageId")
	private String imageId;
	
	@JsonProperty("vimId")
	private String vimId;
	
	@JsonProperty("vimUser")
	private String vimUser;
	
	@JsonProperty("tenant")
	private String tenant;
	
	@JsonProperty("status")
	private String status;

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
	}

	public String getVimUser() {
		return vimUser;
	}

	public void setVimUser(String vimUser) {
		this.vimUser = vimUser;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	
	

}
