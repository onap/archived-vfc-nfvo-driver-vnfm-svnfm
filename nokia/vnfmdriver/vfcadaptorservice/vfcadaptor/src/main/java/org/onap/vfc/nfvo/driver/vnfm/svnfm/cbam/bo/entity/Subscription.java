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

import com.fasterxml.jackson.annotation.JsonProperty;

public class Subscription {
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("lcnApiVersion")
	private String lcnApiVersion;
	
	@JsonProperty("filter")
	private SubscriptionFilter filter;
	
	@JsonProperty("callbackUrl")
	private String callbackUrl;
	
	@JsonProperty("_links")
	private _links _links;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLcnApiVersion() {
		return lcnApiVersion;
	}

	public void setLcnApiVersion(String lcnApiVersion) {
		this.lcnApiVersion = lcnApiVersion;
	}

	public SubscriptionFilter getFilter() {
		return filter;
	}

	public void setFilter(SubscriptionFilter filter) {
		this.filter = filter;
	}

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	public _links get_links() {
		return _links;
	}

	public void set_links(_links _links) {
		this._links = _links;
	}
	
	
	
}
