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
	private String msbIp;
	private int msbPort;
	
	// service name and version of MSB services AAI/LCM/Catalog configured in application.properties
	@Value("${aaiServiceNameInMsb}")
	private String aaiServiceNameInMsb;

	@Value("${aaiVersionInMsb}")
	private String aaiVersionInMsb;

	@Value("${lcmServiceNameInMsb}")
	private String lcmServiceNameInMsb;

	@Value("${lcmVersionInMsb}")
	private String lcmVersionInMsb;

	@Value("${catalogServiceNameInMsb}")
	private String catalogServiceNameInMsb;

	@Value("${catalogVersionInMsb}")
	private String catalogVersionInMsb;

	//Following uriFront is from msb query
	
	private String aaiUrlInMsb;
	private String aaiApiUriFront;
	
	private String lcmUrlInMsb;
	private String lcmApiUriFront;
	
	private String catalogUrlInMsb;
	private String catalogApiUriFront;
	
	//cbamApiFront is from aai query
	@Value("${cbamApiUriFront}")
	private String cbamApiUriFront;
	
	@Value("${cbamUserName}")
	private String cbamUserName;
	
	@Value("${cbamPassword}")
	private String cbamPassword;
	
	
	private String msbApiUriFront;
	
	// for retrieving token from CBAM, configured in application.properties
	@Value("${grantType}")
	private String grantType;

	@Value("${clientId}")
	private String clientId;

	@Value("${clientSecret}")
	private String clientSecret;
	
	// for granting
	@Value("${type}")
	private String type;
	
	@Value("${sizeOfStorage}")
	private String sizeOfStorage;
	
	@Value("${virtualMemSize}")
	private String virtualMemSize;
	
	@Value("${numVirtualCpu}")
	private String numVirtualCpu;
	
	@Value("${vnfdId}")
	private String vnfdId;

	public String getAaiServiceNameInMsb() {
		return aaiServiceNameInMsb;
	}

	public void setAaiServiceNameInMsb(String aaiServiceNameInMsb) {
		this.aaiServiceNameInMsb = aaiServiceNameInMsb;
	}

	public String getAaiVersionInMsb() {
		return aaiVersionInMsb;
	}

	public void setAaiVersionInMsb(String aaiVersionInMsb) {
		this.aaiVersionInMsb = aaiVersionInMsb;
	}

	public String getLcmServiceNameInMsb() {
		return lcmServiceNameInMsb;
	}

	public void setLcmServiceNameInMsb(String lcmServiceNameInMsb) {
		this.lcmServiceNameInMsb = lcmServiceNameInMsb;
	}

	public String getLcmVersionInMsb() {
		return lcmVersionInMsb;
	}

	public void setLcmVersionInMsb(String lcmVersionInMsb) {
		this.lcmVersionInMsb = lcmVersionInMsb;
	}

	public String getCatalogServiceNameInMsb() {
		return catalogServiceNameInMsb;
	}

	public void setCatalogServiceNameInMsb(String catalogServiceNameInMsb) {
		this.catalogServiceNameInMsb = catalogServiceNameInMsb;
	}

	public String getCatalogVersionInMsb() {
		return catalogVersionInMsb;
	}

	public void setCatalogVersionInMsb(String catalogVersionInMsb) {
		this.catalogVersionInMsb = catalogVersionInMsb;
	}

	public String getAaiApiUriFront() {
		return aaiApiUriFront;
	}

	public void setAaiApiUriFront(String aaiApiUriFront) {
		this.aaiApiUriFront = aaiApiUriFront;
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

	public String getMsbIp() {
		return msbIp;
	}

	public void setMsbIp(String msbIp) {
		this.msbIp = msbIp;
	}

	public int getMsbPort() {
		return msbPort;
	}

	public void setMsbPort(int msbPort) {
		this.msbPort = msbPort;
	}

	public String getAaiUrlInMsb() {
		return aaiUrlInMsb;
	}

	public void setAaiUrlInMsb(String aaiUrlInMsb) {
		this.aaiUrlInMsb = aaiUrlInMsb;
	}

	public String getCatalogUrlInMsb() {
		return catalogUrlInMsb;
	}

	public void setCatalogUrlInMsb(String catalogUrlInMsb) {
		this.catalogUrlInMsb = catalogUrlInMsb;
	}

	public String getLcmUrlInMsb() {
		return lcmUrlInMsb;
	}

	public void setLcmUrlInMsb(String lcmUrlInMsb) {
		this.lcmUrlInMsb = lcmUrlInMsb;
	}

	public String getLcmApiUriFront() {
		return lcmApiUriFront;
	}

	public void setLcmApiUriFront(String lcmApiUriFront) {
		this.lcmApiUriFront = lcmApiUriFront;
	}

	public String getMsbApiUriFront() {
		return msbApiUriFront;
	}

	public void setMsbApiUriFront(String msbApiUriFront) {
		this.msbApiUriFront = msbApiUriFront;
	}

	public String getCbamUserName() {
		return cbamUserName;
	}

	public void setCbamUserName(String cbamUserName) {
		this.cbamUserName = cbamUserName;
	}

	public String getCbamPassword() {
		return cbamPassword;
	}

	public void setCbamPassword(String cbamPassword) {
		this.cbamPassword = cbamPassword;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSizeOfStorage() {
		return sizeOfStorage;
	}

	public void setSizeOfStorage(String sizeOfStorage) {
		this.sizeOfStorage = sizeOfStorage;
	}

	public String getVirtualMemSize() {
		return virtualMemSize;
	}

	public void setVirtualMemSize(String virtualMemSize) {
		this.virtualMemSize = virtualMemSize;
	}

	public String getNumVirtualCpu() {
		return numVirtualCpu;
	}

	public void setNumVirtualCpu(String numVirtualCpu) {
		this.numVirtualCpu = numVirtualCpu;
	}

	public String getVnfdId() {
		return vnfdId;
	}

	public void setVnfdId(String vnfdId) {
		this.vnfdId = vnfdId;
	}


}
