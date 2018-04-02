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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean;

import java.io.Serializable;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;

//@Entity
//@Table(name = "vnfm_subscription_info")
public class VnfmSubscriptionInfo implements Serializable {
	private static final long serialVersionUID = -288015953900428312L;

//	@Id
//	@Column(name = "id")
	private String id;
	
	private String driverCallbackUrl;
	
	private String nslcmCallbackUrl;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDriverCallbackUrl() {
		return driverCallbackUrl;
	}

	public void setDriverCallbackUrl(String driverCallbackUrl) {
		this.driverCallbackUrl = driverCallbackUrl;
	}

	public String getNslcmCallbackUrl() {
		return nslcmCallbackUrl;
	}

	public void setNslcmCallbackUrl(String nslcmCallbackUrl) {
		this.nslcmCallbackUrl = nslcmCallbackUrl;
	}
	
	@Override
	public String toString() {
		return " VnfmSubscriptionInfo: [ " + super.toString() + ", id = " + id + ", driverCallbackUrl = " + driverCallbackUrl +", nslcmCallbackUrl = " + nslcmCallbackUrl + "]";
	}
}
