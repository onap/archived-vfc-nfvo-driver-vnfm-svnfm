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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo;

import java.util.List;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.aai.bo.entity.EsrSystemInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class AaiVnfmInfo {
	@SerializedName("vnfm-id")
	@JsonProperty("vnfm-id")
	private String vnfmId;
	
	@SerializedName("vim-id")
	@JsonProperty("vim-id")
	private String vimId;
	
	@SerializedName("certificate-url")
	@JsonProperty("certificate-url")
	private String certificateUrl;
	
	@SerializedName("resource-version")
	@JsonProperty("resource-version")
	private String resourceVersion;
	
	@SerializedName("esr-system-info")
	@JsonProperty("esr-system-info")
	private List<EsrSystemInfo> esrSystemInfoList;

	public String getVnfmId() {
		return vnfmId;
	}

	public void setVnfmId(String vnfmId) {
		this.vnfmId = vnfmId;
	}

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
	}

	public String getCertificateUrl() {
		return certificateUrl;
	}

	public void setCertificateUrl(String certificateUrl) {
		this.certificateUrl = certificateUrl;
	}

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	public List<EsrSystemInfo> getEsrSystemInfoList() {
		return esrSystemInfoList;
	}

	public void setEsrSystemInfoList(List<EsrSystemInfo> esrSystemInfoList) {
		this.esrSystemInfoList = esrSystemInfoList;
	}
}
