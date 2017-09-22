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
package com.nokia.vfcadaptor.vnfmdriver.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


public class ExtVirtualLinkData {
	@JsonProperty("vlInstanceId")
	private String vlInstanceId;
	@JsonProperty("vim")
	private VimInfo vim;
	@JsonProperty("networkId")
	private String networkId;
	@JsonProperty("cpdId")
	private String cpdId;
	public String getVlInstanceId() {
		return vlInstanceId;
	}
	public void setVlInstanceId(String vlInstanceId) {
		this.vlInstanceId = vlInstanceId;
	}
	public VimInfo getVim() {
		return vim;
	}
	public void setVim(VimInfo vim) {
		this.vim = vim;
	}
	public String getNetworkId() {
		return networkId;
	}
	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}
	public String getCpdId() {
		return cpdId;
	}
	public void setCpdId(String cpdId) {
		this.cpdId = cpdId;
	}
	
	

}
