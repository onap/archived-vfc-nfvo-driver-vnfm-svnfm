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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdaptorEnv {
	@Value("${nslcmApiUriFront}")
	private String nslcmApiUriFront;
	@Value("${catalogApiUriFront}")
	private String catalogApiUriFront;
	@Value("${cbamApiUriFront}")
	private String cbamApiUriFront;
	
	//for retrieving token from CBAM
	@Value("${grantType}")
	private String grantType;
	@Value("${clientId}")
	private String clientId;
	@Value("${clientSecret}")
	private String clientSecret;
	
	public String getNslcmApiUriFront() {
		return nslcmApiUriFront;
	}
	public void setNslcmApiUriFront(String nslcmApiUriFront) {
		this.nslcmApiUriFront = nslcmApiUriFront;
	}
	public String getCatalogApiUriFront() {
		return catalogApiUriFront;
	}
	public void setCatalogApiUriFront(String catalogApiUriFront) {
		this.catalogApiUriFront = catalogApiUriFront;
	}
	public String getCbamApiUriFront() {
		return cbamApiUriFront;
	}
	public void setCbamApiUriFront(String cbamApiUriFront) {
		this.cbamApiUriFront = cbamApiUriFront;
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
	
}
