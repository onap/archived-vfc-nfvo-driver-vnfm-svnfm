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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.vnfmdriver.bo;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.Subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateSubscriptionResponse extends Subscription{
	//the attribute callbackUri is for SOL003 driver, while callbackUrl is for CBAM
		@JsonProperty("callbackUri")
		private String callbackUri;

		public String getCallbackUri() {
			return callbackUri;
		}

		public void setCallbackUri(String callbackUri) {
			this.callbackUri = callbackUri;
		}
}
