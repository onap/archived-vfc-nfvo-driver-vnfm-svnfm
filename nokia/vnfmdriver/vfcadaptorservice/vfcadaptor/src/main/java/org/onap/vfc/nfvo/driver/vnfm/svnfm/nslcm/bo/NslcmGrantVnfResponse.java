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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo;

import java.util.List;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.ResourceDefinition;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.VimAssets;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.NslcmVimInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NslcmGrantVnfResponse {
	
	@JsonProperty("vim")
	private List<NslcmVimInfo> vim;
	@JsonProperty("vimAssets")
	private VimAssets vimAssets;
	
	@JsonProperty("additionalParam")
	private List<ResourceDefinition> additionalParam;

	

	public List<NslcmVimInfo> getVim() {
		return vim;
	}

	public void setVim(List<NslcmVimInfo> vim) {
		this.vim = vim;
	}

	public VimAssets getVimAssets() {
		return vimAssets;
	}

	public void setVimAssets(VimAssets vimAssets) {
		this.vimAssets = vimAssets;
	}

	public List<ResourceDefinition> getAdditionalParam() {
		return additionalParam;
	}

	public void setAdditionalParam(List<ResourceDefinition> additionalParam) {
		this.additionalParam = additionalParam;
	}

   
	
	
	
	
}
