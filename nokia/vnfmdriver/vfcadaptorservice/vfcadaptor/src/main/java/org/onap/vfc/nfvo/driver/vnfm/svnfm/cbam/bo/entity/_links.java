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
	private Link cancel;
	@JsonProperty("operationParams")
	private Link operationParams;
	@JsonProperty("endNotification")
	private Link endNotification;
	@JsonProperty("self")
	private Link self;
	@JsonProperty("additionalData")
	private Link additionalData;
	@JsonProperty("list")
	private Link list;
	@JsonProperty("vnf") 
	private Link vnf;
	public Link getCancel() {
		return cancel;
	}
	public void setCancel(Link cancel) {
		this.cancel = cancel;
	}
	public Link getOperationParams() {
		return operationParams;
	}
	public void setOperationParams(Link operationParams) {
		this.operationParams = operationParams;
	}
	public Link getEndNotification() {
		return endNotification;
	}
	public void setEndNotification(Link endNotification) {
		this.endNotification = endNotification;
	}
	public Link getSelf() {
		return self;
	}
	public void setSelf(Link self) {
		this.self = self;
	}
	public Link getAdditionalData() {
		return additionalData;
	}
	public void setAdditionalData(Link additionalData) {
		this.additionalData = additionalData;
	}
	public Link getList() {
		return list;
	}
	public void setList(Link list) {
		this.list = list;
	}
	public Link getVnf() {
		return vnf;
	}
	public void setVnf(Link vnf) {
		this.vnf = vnf;
	}
	
	
	
	
	
	

}
