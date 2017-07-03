/*
 * Copyright 2016-2017 Huawei Technologies Co., Ltd.
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

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openo.nfvo.vnfmadapter.common.VnfmJsonUtil;
import org.openo.nfvo.vnfmadapter.common.VnfmUtil;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.process.VnfResourceMgr;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;

public class VnfResourceRoaTest {

    private VnfResourceRoa vnfResourceRoa;

    private VnfResourceMgr vnfResourceMgr;

    @Before
    public void setUp() {
        vnfResourceRoa = new VnfResourceRoa();
        vnfResourceMgr = new VnfResourceMgr();
        vnfResourceRoa.setVnfResourceMgr(vnfResourceMgr);
    }

    @After
    public void tearDown() {
        vnfResourceRoa = null;
        vnfResourceMgr = null;
    }

    @Test
    public void testGrantVnfResByDataObjectNull() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();
        new MockUp<VnfmJsonUtil>() {

            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                return null;
            }
        };

        String result = vnfResourceRoa.grantVnfRes(mockInstance, "vnfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        restJson.put("data", "Params error");
        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGrantVnfResByGrantObjNull() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();
        final JSONObject dataObject = new JSONObject();
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                return (T)dataObject;
            }
        };
        new MockUp<JSONObject>() {

            @Mock
            public JSONObject getJSONObject(String key) {
                if(key == "grant") {
                    return null;
                }
                return dataObject;
            }
        };

        String result = vnfResourceRoa.grantVnfRes(mockInstance, "vnfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        restJson.put("data", "Grant param error");
        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGrantVnfRes() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {

            @Mock
            public String getHeader(String name) {
                return "127.0.0.1";
            }
        };
        HttpServletRequest mockInstance = proxyStub.getMockInstance();
        final JSONObject dataObject = new JSONObject();
        JSONObject grant = new JSONObject();
        grant.put("project_id", "project_id");
        dataObject.put("grant", grant);
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                return (T)dataObject;
            }
        };
        new MockUp<VnfmUtil>() {

            @Mock
            public String getVnfmIdByIp(String ip) {
                return "vnfmId";
            }
        };

        new MockUp<VnfResourceMgr>() {

            @Mock
            public JSONObject grantVnfResource(JSONObject vnfObj, String vnfId, String vnfmId) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("retCode", Constant.REST_SUCCESS);
                JSONObject data = new JSONObject();
                data.put("data", "success");
                resultJson.put("data", data);
                return resultJson;
            }
        };
        String result = vnfResourceRoa.grantVnfRes(mockInstance, "vnfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        JSONObject data = new JSONObject();
        data.put("data", "success");
        restJson.put("data", data);
        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGrantVnfResByFail() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {

            @Mock
            public String getHeader(String name) {
                return "127.0.0.1";
            }
        };
        HttpServletRequest mockInstance = proxyStub.getMockInstance();
        final JSONObject dataObject = new JSONObject();
        JSONObject grant = new JSONObject();
        grant.put("project_id", "project_id");
        dataObject.put("grant", grant);
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                return (T)dataObject;
            }
        };
        new MockUp<VnfmUtil>() {

            @Mock
            public String getVnfmIdByIp(String ip) {
                return "vnfmId";
            }
        };

        new MockUp<VnfResourceMgr>() {

            @Mock
            public JSONObject grantVnfResource(JSONObject vnfObj, String vnfId, String vnfmId) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("retCode", Constant.REST_FAIL);
                resultJson.put("data", "Fail!");
                return resultJson;
            }
        };
        String result = vnfResourceRoa.grantVnfRes(mockInstance, "vnfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        restJson.put("data", "Fail!");
        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGrantVnfResByDataNull() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {

            @Mock
            public String getHeader(String name) {
                return "127.0.0.1";
            }
        };
        HttpServletRequest mockInstance = proxyStub.getMockInstance();
        final JSONObject dataObject = new JSONObject();
        JSONObject grant = new JSONObject();
        grant.put("project_id", "project_id");
        dataObject.put("grant", grant);
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                return (T)dataObject;
            }
        };
        new MockUp<VnfmUtil>() {

            @Mock
            public String getVnfmIdByIp(String ip) {
                return "vnfmId";
            }
        };

        new MockUp<VnfResourceMgr>() {

            @Mock
            public JSONObject grantVnfResource(JSONObject vnfObj, String vnfId, String vnfmId) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("retCode", Constant.REST_FAIL);
                return resultJson;
            }
        };
        String result = vnfResourceRoa.grantVnfRes(mockInstance, "vnfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testNotify() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {

            @Mock
            public String getHeader(String name) {
                return "127.0.0.1";
            }
        };
        HttpServletRequest mockInstance = proxyStub.getMockInstance();
        String result = vnfResourceRoa.notify(mockInstance);

        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
        assertEquals(restJson.toString(), result);
    }
}
