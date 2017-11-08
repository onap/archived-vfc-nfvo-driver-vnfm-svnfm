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

import java.util.ArrayList;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.VnfProperty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CBAMModifyVnfRequest {
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("metadata")
	private String metadata;
	
	@JsonProperty("extensions")
	private ArrayList<VnfProperty> extensions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public ArrayList<VnfProperty> getExtensions() {
		return extensions;
	}

	public void setExtensions(ArrayList<VnfProperty> extensions) {
		this.extensions = extensions;
	}
	
}
