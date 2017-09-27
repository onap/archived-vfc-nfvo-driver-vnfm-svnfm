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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo.entity;



import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseHistoryList {

	
	@JsonProperty("status")
	private String  status;//started;processing;finished;error
	
	@JsonProperty("progress")
	private Integer  progress;//progress (1-100)
	
	@JsonProperty("statusDescription")
	private String  statusDescription;
	
	@JsonProperty("errorCode")
	private Integer errorCode;
	
	@JsonProperty("responseId")
	private Integer responseId;
	
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public String getStatusDescription() {
		return statusDescription;
	}

	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public Integer getResponseId() {
		return responseId;
	}

	public void setResponseId(Integer responseId) {
		this.responseId = responseId;
	}

	

	
	
	
	
	
}
