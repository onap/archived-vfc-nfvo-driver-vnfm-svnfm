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

import com.fasterxml.jackson.annotation.JsonProperty;

public class _links {
	@JsonProperty("cancel")
	private String cancel;
	@JsonProperty("operationParams")
	private String operationParams;
	@JsonProperty("endNotification")
	private String endNotification;
	@JsonProperty("self")
	private String self;
	@JsonProperty("additionalData")
	private String additionalData;
	@JsonProperty("list")
	private String list;
	@JsonProperty("vnf") 
	private String vnf;
	public String getCancel() {
		return cancel;
	}
	public void setCancel(String cancel) {
		this.cancel = cancel;
	}
	public String getOperationParams() {
		return operationParams;
	}
	public void setOperationParams(String operationParams) {
		this.operationParams = operationParams;
	}
	public String getEndNotification() {
		return endNotification;
	}
	public void setEndNotification(String endNotification) {
		this.endNotification = endNotification;
	}
	public String getSelf() {
		return self;
	}
	public void setSelf(String self) {
		this.self = self;
	}
	public String getAdditionalData() {
		return additionalData;
	}
	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}
	public String getList() {
		return list;
	}
	public void setList(String list) {
		this.list = list;
	}
	public String getVnf() {
		return vnf;
	}
	public void setVnf(String vnf) {
		this.vnf = vnf;
	}
	
	
	
	
	

}
