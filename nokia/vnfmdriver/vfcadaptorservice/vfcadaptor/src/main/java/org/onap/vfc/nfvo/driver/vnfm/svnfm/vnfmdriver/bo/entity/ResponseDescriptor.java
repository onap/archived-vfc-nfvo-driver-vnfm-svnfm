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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseDescriptor {

	
	@JsonProperty("status")
	private String  status;//started;processing;finished;error
	
	@JsonProperty("progress")
	private String  progress;//progress (1-100)
	
	@JsonProperty("statusDescription")
	private String  statusDescription;
	
	@JsonProperty("errorCode")
	private String errorCode;
	
	@JsonProperty("responseId")
	private String responseId;
	
	@JsonProperty("responseHistoryList")
	private List<ResponseHistoryList> responseHistoryList;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProgress() {
		return progress;
	}


	public void setProgress(String progr) {
		  progress = progr;
	}

	public String getStatusDescription() {
		return statusDescription;
	}

	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getResponseId() {
		return responseId;
	}

	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	public List<ResponseHistoryList> getResponseHistoryList() {
		return responseHistoryList;
	}

	public void setResponseHistoryList(List<ResponseHistoryList> responseHistoryList) {
		this.responseHistoryList = responseHistoryList;
	}

	

	
	
	
	
	
}
