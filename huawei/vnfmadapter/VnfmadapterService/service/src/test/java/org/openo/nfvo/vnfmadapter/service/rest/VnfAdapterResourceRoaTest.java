/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

package org.openo.nfvo.vnfmadapter.service.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openo.nfvo.vnfmadapter.service.adapter.impl.AdapterResourceManager;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version NFVO 0.5 Jan 13, 2017
 */
public class VnfAdapterResourceRoaTest {

    @Test
    public void testGetAllCloudInfo() {
        new MockUp<AdapterResourceManager>() {

            @Mock
            public JSONObject getAllCloud(String url) {
                JSONObject resultObj = new JSONObject();
                resultObj.put("dn", "test");
                return resultObj;
            }
        };

        VnfAdapterResourceRoa vnfAdapter = new VnfAdapterResourceRoa();
        String result = vnfAdapter.getAllCloudInfo(null);
        assertEquals("test", result);
    }

}
