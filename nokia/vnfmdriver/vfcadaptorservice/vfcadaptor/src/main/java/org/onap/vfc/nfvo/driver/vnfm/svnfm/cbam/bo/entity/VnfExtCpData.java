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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VnfExtCpData {
	@JsonProperty("cpdId")
	private String cpdId;
	
	@JsonProperty("addresses")
	private List<NetworkAddress> addresses=new ArrayList<NetworkAddress>();
	
	@JsonProperty("numDynamicAddresses")
	private Integer numDynamicAddresses;

	public String getCpdId() {
		return cpdId;
	}

	public void setCpdId(String cpdId) {
		this.cpdId = cpdId;
	}

	public List<NetworkAddress> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<NetworkAddress> addresses) {
		this.addresses = addresses;
	}

	public Integer getNumDynamicAddresses() {
		return numDynamicAddresses;
	}

	public void setNumDynamicAddresses(Integer numDynamicAddresses) {
		this.numDynamicAddresses = numDynamicAddresses;
	}
	
	

}
