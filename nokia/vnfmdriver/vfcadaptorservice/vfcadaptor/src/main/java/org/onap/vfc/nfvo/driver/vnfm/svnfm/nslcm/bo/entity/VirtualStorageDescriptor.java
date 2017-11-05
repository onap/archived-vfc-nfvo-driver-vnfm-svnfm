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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VirtualStorageDescriptor {
	@JsonProperty("sizeOfStorage")
	private int sizeOfStorage;
	@JsonProperty("typeOfStorage")
	private String typeOfStorage;
	@JsonProperty("swImageDescriptor")
	private String swImageDescriptor;

	public int getSizeOfStorage() {
		return sizeOfStorage;
	}

	public void setSizeOfStorage(int sizeOfStorage) {
		this.sizeOfStorage = sizeOfStorage;
	}

	public String getTypeOfStorage() {
		return typeOfStorage;
	}

	public void setTypeOfStorage(String typeOfStorage) {
		this.typeOfStorage = typeOfStorage;
	}

	public String getSwImageDescriptor() {
		return swImageDescriptor;
	}

	public void setSwImageDescriptor(String swImageDescriptor) {
		this.swImageDescriptor = swImageDescriptor;
	}

}
