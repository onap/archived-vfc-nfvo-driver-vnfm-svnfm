/*
 * Copyright 2018 Huawei Technologies Co., Ltd.
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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.vnf;

import org.junit.Test;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ScaleManagerTest {

	
	@Test
	public void beforeScaleOut() {
		JSONObject queryVms = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("vms", new JSONArray());
		queryVms.put("data",data);
		 ScaleManager.beforeScaleOut(queryVms, "vnfId");;
		 
	}
	
	@Test
	public void beforeScaleIn() {
		JSONObject queryVms = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("vms", new JSONArray());
		queryVms.put("data",data);
		 ScaleManager.beforeScaleIn(queryVms, "vnfId");;
		 
	}
	
}
