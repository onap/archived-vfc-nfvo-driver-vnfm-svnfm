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

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfNotificationType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CBAMVnfNotificationRequest {
	@JsonProperty("notificationType")
	private VnfNotificationType notificationType;
	
	@JsonProperty("subscriptionId")
	private String subscriptionId;
	
	@JsonProperty("timestamp")
	private String timestamp;
	
	@JsonProperty("vnfInstanceId")
	private String vnfInstanceId;

	public VnfNotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(VnfNotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	

   
}
