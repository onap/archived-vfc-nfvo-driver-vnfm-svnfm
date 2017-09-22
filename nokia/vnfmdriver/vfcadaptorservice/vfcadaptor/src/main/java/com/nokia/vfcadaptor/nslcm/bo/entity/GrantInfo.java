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
package com.nokia.vfcadaptor.nslcm.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrantInfo {
  
	@JsonProperty("resourceDefinitionId")
	private String resourceDefinitionId;
	
	@JsonProperty("reservationId")
	private String reservationId;
	
	@JsonProperty("vimId")
	private String vimId;

	@JsonProperty("resourceProviderId")
	private String resourceProviderId;
	
	@JsonProperty("zoneId")
	private String zoneId;

	public String getResourceDefinitionId() {
		return resourceDefinitionId;
	}

	public void setResourceDefinitionId(String resourceDefinitionId) {
		this.resourceDefinitionId = resourceDefinitionId;
	}

	public String getReservationId() {
		return reservationId;
	}

	public void setReservationId(String reservationId) {
		this.reservationId = reservationId;
	}

	public String getVimId() {
		return vimId;
	}

	public void setVimId(String vimId) {
		this.vimId = vimId;
	}

	public String getResourceProviderId() {
		return resourceProviderId;
	}

	public void setResourceProviderId(String resourceProviderId) {
		this.resourceProviderId = resourceProviderId;
	}

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}
	
	
	
	
	
	
	
}
