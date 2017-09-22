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
package com.nokia.vfcadaptor.catalog.bo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nokia.vfcadaptor.catalog.bo.entity.ImageInfo;
import com.nokia.vfcadaptor.catalog.bo.entity.VnfInstanceInfo;
import com.nokia.vfcadaptor.nslcm.bo.entity.VnfPackageInfo;

public class CatalogQueryVnfResponse {
	@JsonProperty("csarId")
	private String csarId;
	
	@JsonProperty("packageInfo")
	private VnfPackageInfo packageInfo;
	
	@JsonProperty("imageInfo")
	private List<ImageInfo> imageInfo;
	
	@JsonProperty("vnfInstanceInfo")
	private List<VnfInstanceInfo> vnfInstanceInfo;

	public String getCsarId() {
		return csarId;
	}

	public void setCsarId(String csarId) {
		this.csarId = csarId;
	}

	

	public VnfPackageInfo getPackageInfo() {
		return packageInfo;
	}

	public void setPackageInfo(VnfPackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}

	public List<ImageInfo> getImageInfo() {
		return imageInfo;
	}

	public void setImageInfo(List<ImageInfo> imageInfo) {
		this.imageInfo = imageInfo;
	}

	public List<VnfInstanceInfo> getVnfInstanceInfo() {
		return vnfInstanceInfo;
	}

	public void setVnfInstanceInfo(List<VnfInstanceInfo> vnfInstanceInfo) {
		this.vnfInstanceInfo = vnfInstanceInfo;
	}


	
	
	
	
	
}
