
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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CBAMTerminateVnfRequest {
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("description")
	private String description;

	@JsonProperty("terminationType")
	private CommonEnum.TerminationType terminationType;
	
	@JsonProperty("gracefulTerminationTimeout")
	private Integer gracefulTerminationTimeout;
	
	@JsonProperty("additionalParams")
	private Object additionalParams;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CommonEnum.TerminationType getTerminationType() {
		return terminationType;
	}

	public void setTerminationType(CommonEnum.TerminationType terminationType) {
		this.terminationType = terminationType;
	}

	public Integer getGracefulTerminationTimeout() {
		return gracefulTerminationTimeout;
	}

	public void setGracefulTerminationTimeout(Integer gracefulTerminationTimeout) {
		this.gracefulTerminationTimeout = gracefulTerminationTimeout;
	}

	public Object getAdditionalParams() {
		return additionalParams;
	}

	public void setAdditionalParams(Object additionalParams) {
		this.additionalParams = additionalParams;
	}

	public enum TerminationType{
		GRACEFUL, FORCEFUL
	}

}
