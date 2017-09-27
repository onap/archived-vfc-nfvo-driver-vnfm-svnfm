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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.constant.CommonEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OperationExecution {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;
	
	@JsonProperty("operationType")
	private OperationType operationType;
	
	@JsonProperty("operationName")
	private String operationName;
	
	@JsonProperty("status")
	private CommonEnum.OperationStatus status;
	
	@JsonProperty("isCancelPending")
	private boolean isCancelPending;
	
	@JsonProperty("CancelMode")
	private CancelMode CancelMode;
	
	@JsonProperty("error")
	private ProblemDetails error;
	
	@JsonProperty("startTime")
	private String startTime;
	
	@JsonProperty("finishTime")
	private String finishTime;
	
	@JsonProperty("grantId")
	private String grantId;
	
	@JsonProperty("operationParams")
	private Object operationParams;
	
	@JsonProperty("additionalData")
	private Object additionalData;
	
	@JsonProperty("metadata")
	private Object metadata;
	
	@JsonProperty("_links")
	private _links _links;
	
	
	
	
	
	public enum OperationType{
		INSTANTIATE, SCALE, SCALE_TO_LEVEL, MODIFY_INFO, CHANGE_FLAVOUR, OPERATE,
		HEAL, UPGRADE, TERMINATE, OTHER
	}
	
	
	
	public enum CancelMode{
		GRACEFUL, FORCEFUL
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	

	public CommonEnum.OperationStatus getStatus() {
		return status;
	}

	public void setStatus(CommonEnum.OperationStatus status) {
		this.status = status;
	}

	public boolean isCancelPending() {
		return isCancelPending;
	}

	public void setCancelPending(boolean isCancelPending) {
		this.isCancelPending = isCancelPending;
	}

	public CancelMode getCancelMode() {
		return CancelMode;
	}

	public void setCancelMode(CancelMode cancelMode) {
		CancelMode = cancelMode;
	}

	public ProblemDetails getError() {
		return error;
	}

	public void setError(ProblemDetails error) {
		this.error = error;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public String getGrantId() {
		return grantId;
	}

	public void setGrantId(String grantId) {
		this.grantId = grantId;
	}

	public Object getOperationParams() {
		return operationParams;
	}

	public void setOperationParams(Object operationParams) {
		this.operationParams = operationParams;
	}

	public Object getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(Object additionalData) {
		this.additionalData = additionalData;
	}

	public Object getMetadata() {
		return metadata;
	}

	public void setMetadata(Object metadata) {
		this.metadata = metadata;
	}

	public _links get_links() {
		return _links;
	}

	public void set_links(_links _links) {
		this._links = _links;
	}

	
	
}
