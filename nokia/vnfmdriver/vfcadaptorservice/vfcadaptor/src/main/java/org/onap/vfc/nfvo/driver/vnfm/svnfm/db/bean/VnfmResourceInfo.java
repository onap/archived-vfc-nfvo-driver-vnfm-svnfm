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

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vnfm_resource_record")
public class VnfmResourceInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "type")
	private ResourceType type;
	
	@Column(name = "resourceDefinitionId")
	private String resourceDefinitionId;
	
	@Column(name = "vdu")
	private String vdu;

	@Column(name = "status")
	private String status;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ResourceType getType() {
		return type;
	}

	public void setType(ResourceType type) {
		this.type = type;
	}

	public String getResourceDefinitionId() {
		return resourceDefinitionId;
	}

	public void setResourceDefinitionId(String resourceDefinitionId) {
		this.resourceDefinitionId = resourceDefinitionId;
	}

	public String getVdu() {
		return vdu;
	}

	public void setVdu(String vdu) {
		this.vdu = vdu;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
