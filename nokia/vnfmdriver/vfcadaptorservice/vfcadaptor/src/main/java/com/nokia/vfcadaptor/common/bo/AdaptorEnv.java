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

package com.nokia.vfcadaptor.common.bo;

public class AdaptorEnv {
	private String nslcmIp;
	private String nslcmPort;
	
	//for retrieving token
	private String cbamIp;
	
	private String grantType;
	private String clientId;
	private String clientSecret;
	
	private String catalogIp;
	private String catalogPort;

	public String getCbamIp() {
		return cbamIp;
	}

	public void setCbamIp(String cbamIp) {
		this.cbamIp = cbamIp;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getCatalogIp() {
		return catalogIp;
	}

	public void setCatalogIp(String catalogIp) {
		this.catalogIp = catalogIp;
	}

	public String getCatalogPort() {
		return catalogPort;
	}

	public void setCatalogPort(String catalogPort) {
		this.catalogPort = catalogPort;
	}

	public String getNslcmIp() {
		return nslcmIp;
	}

	public void setNslcmIp(String nslcmIp) {
		this.nslcmIp = nslcmIp;
	}

	public String getNslcmPort() {
		return nslcmPort;
	}

	public void setNslcmPort(String nslcmPort) {
		this.nslcmPort = nslcmPort;
	}
	
	
}
