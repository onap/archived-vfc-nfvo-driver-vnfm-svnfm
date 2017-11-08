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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.TerminationType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TerminateVnfRequest {
	
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;
	
	@JsonProperty("terminationType")
	private TerminationType terminationType;

	@JsonProperty("gracefulTerminationTimeout")
	private Integer gracefulTerminationTimeout;

	
	public TerminationType getTerminationType() {
		return terminationType;
	}

	public void setTerminationType(TerminationType terminationType) {
		this.terminationType = terminationType;
	}

	public Integer getGracefulTerminationTimeout() {
		return gracefulTerminationTimeout;
	}

	public void setGracefulTerminationTimeout(Integer gracefulTerminationTimeout) {
		this.gracefulTerminationTimeout = gracefulTerminationTimeout;
	}

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	
	
}
