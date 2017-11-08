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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo;


import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.ScaleType;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity.AdditionalParam;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScaleVnfRequest {
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;
	
	@JsonProperty("type")
	private ScaleType type;
	
	@JsonProperty("aspectId")
	private String aspectId;
	
	@JsonProperty("numberOfSteps")
	private Integer numberOfSteps;
	
	@JsonProperty("additionalParam")
	private AdditionalParam additionalParam;
	
	
	public String getAspectId() {
		return aspectId;
	}

	public void setAspectId(String aspectId) {
		this.aspectId = aspectId;
	}

	public Integer getNumberOfSteps() {
		return numberOfSteps;
	}

	public void setNumberOfSteps(Integer numberOfSteps) {
		this.numberOfSteps = numberOfSteps;
	}

	public AdditionalParam getAdditionalParam() {
		return additionalParam;
	}

	public void setAdditionalParam(AdditionalParam additionalParam) {
		this.additionalParam = additionalParam;
	}

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public ScaleType getType() {
		return type;
	}

	public void setType(ScaleType type) {
		this.type = type;
	}

	

	


	
	
	
	
}
