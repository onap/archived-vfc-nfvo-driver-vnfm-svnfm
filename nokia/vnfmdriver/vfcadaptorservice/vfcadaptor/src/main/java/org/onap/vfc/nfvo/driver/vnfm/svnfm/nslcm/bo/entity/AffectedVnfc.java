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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AffectedVnfc {
	@JsonIgnore
	private String vnfInstanceId;
	
	@JsonProperty("vnfcInstanceId")
	private String vnfcInstanceId;
	
	@JsonProperty("vduId")
	private String vduId;
	
	@JsonProperty("changeType")
	private CommonEnum.AffectchangeType changeType;
	
	@JsonProperty("vimid")
	private String vimid;
	
	@JsonProperty("vmid")
	private String vmid;

	public String getVnfcInstanceId() {
		return vnfcInstanceId;
	}

	public void setVnfcInstanceId(String vnfcInstanceId) {
		this.vnfcInstanceId = vnfcInstanceId;
	}

	public String getVduId() {
		return vduId;
	}

	public void setVduId(String vduId) {
		this.vduId = vduId;
	}

	public CommonEnum.AffectchangeType getChangeType() {
		return changeType;
	}

	public void setChangeType(CommonEnum.AffectchangeType changeType) {
		this.changeType = changeType;
	}

	public String getVimid() {
		return vimid;
	}

	public void setVimid(String vimid) {
		this.vimid = vimid;
	}

	public String getVmid() {
		return vmid;
	}

	public void setVmid(String vmid) {
		this.vmid = vmid;
	}

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}
	
	
	
}
