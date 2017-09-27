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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.KeyValuePair;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NslcmGrantVnfRequest {
	
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;
	
	@JsonProperty("lifecycleOperation")
	private CommonEnum.LifecycleOperation lifecycleOperation;
	
	@JsonProperty("jobId")
	private String jobId;
	
	@JsonProperty("addResource")
	private List<ResourceDefinition> addResource;
	
	@JsonProperty("removeResource")
	private List<ResourceDefinition> removeResource;
	
	@JsonProperty("additionalParam")
	private List<KeyValuePair> additionalParam;

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}


	public CommonEnum.LifecycleOperation getLifecycleOperation() {
		return lifecycleOperation;
	}

	public void setLifecycleOperation(CommonEnum.LifecycleOperation lifecycleOperation) {
		this.lifecycleOperation = lifecycleOperation;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public List<KeyValuePair> getAdditionalParam() {
		return additionalParam;
	}

	public void setAdditionalParam(List<KeyValuePair> additionalParam) {
		this.additionalParam = additionalParam;
	}

	public List<ResourceDefinition> getAddResource() {
		return addResource;
	}

	public void setAddResource(List<ResourceDefinition> addResource) {
		this.addResource = addResource;
	}

	public List<ResourceDefinition> getRemoveResource() {
		return removeResource;
	}

	public void setRemoveResource(List<ResourceDefinition> removeResource) {
		this.removeResource = removeResource;
	}
	
	
	
	
}
