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

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdditionalParam {
	@JsonProperty("vnfmid")
	private String vnfmid;
	@JsonProperty("vimid")
	private String vimid;
	@JsonProperty("tenant")
	private String tenant;

	public String getVnfmid() {
		return vnfmid;
	}

	public void setVnfmid(String vnfmid) {
		this.vnfmid = vnfmid;
	}

	public String getVimid() {
		return vimid;
	}

	public void setVimid(String vimid) {
		this.vimid = vimid;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

}
