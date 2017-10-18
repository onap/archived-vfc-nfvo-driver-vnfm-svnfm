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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo;

import java.util.List;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVirtualLink;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVirtualStorage;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NslcmNotifyLCMEventsRequest {
	
	@JsonProperty("status")
	private CommonEnum.status status;
	
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;
	@JsonProperty("operation")
	private String operation;
	@JsonProperty("jobId")
	private String jobId;
	@JsonProperty("affectedVnfc")
	private List<AffectedVnfc> affectedVnfc;
	@JsonProperty("affectedVl")
	private List<AffectedVirtualLink> affectedVl;
	@JsonProperty("affectedVirtualStorage")
	private List<AffectedVirtualStorage> affectedVirtualStorage;
	public CommonEnum.status getStatus() {
		return status;
	}
	public void setStatus(CommonEnum.status status) {
		this.status = status;
	}
	public String getVnfInstanceId() {
		return vnfInstanceId;
	}
	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public List<AffectedVnfc> getAffectedVnfc() {
		return affectedVnfc;
	}
	public void setAffectedVnfc(List<AffectedVnfc> affectedVnfc) {
		this.affectedVnfc = affectedVnfc;
	}
	public List<AffectedVirtualLink> getAffectedVl() {
		return affectedVl;
	}
	public void setAffectedVl(List<AffectedVirtualLink> affectedVl) {
		this.affectedVl = affectedVl;
	}
	public List<AffectedVirtualStorage> getAffectedVirtualStorage() {
		return affectedVirtualStorage;
	}
	public void setAffectedVirtualStorage(List<AffectedVirtualStorage> affectedVirtualStorage) {
		this.affectedVirtualStorage = affectedVirtualStorage;
	}
	
	
	
	
}
