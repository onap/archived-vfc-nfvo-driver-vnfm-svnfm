/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
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
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openo.nfvo.vnfmadapter.common.VnfmJsonUtil;
import org.openo.nfvo.vnfmadapter.service.constant.Constant;
import org.openo.nfvo.vnfmadapter.service.process.AuthMgr;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;

public class AuthRoaTest {

    private AuthRoa authRoa;

    private AuthMgr authMgr;

    @Before
    public void setUp() {
        authRoa = new AuthRoa();
        authMgr = new AuthMgr();
        authRoa.setAuthMgr(authMgr);
    }

    @After
    public void tearDown() {
        authRoa = null;
        authMgr = null;
    }

    @Test
    public void testAuthTokenBySubJsonObjectNull() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();
        new MockUp<VnfmJsonUtil>() {

            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                return null;
            }
        };

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        String result = authRoa.authToken(mockInstance, mockResInstance);

        assertEquals("Login params insufficient", result);
    }

    @Test
    public void testAuthTokenByFail() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                JSONObject subJsonObject = new JSONObject();
                return (T)subJsonObject;
            }
        };
        new MockUp<AuthMgr>() {

            @Mock
            public JSONObject authToken(JSONObject params) {
                JSONObject restJson = new JSONObject();
                restJson.put("retCode", Constant.REST_FAIL);
                restJson.put("data", "Fail!");
                return restJson;
            }
        };
        String result = authRoa.authToken(mockInstance, mockResInstance);

        assertEquals("{\"Information\": \"Fail!\"}", result);
    }

    @Test
    public void testAuthTokenByHttpInnerError() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                JSONObject subJsonObject = new JSONObject();
                return (T)subJsonObject;
            }
        };
        new MockUp<AuthMgr>() {

            @Mock
            public JSONObject authToken(JSONObject params) {
                JSONObject restJson = new JSONObject();
                restJson.put("retCode", Constant.HTTP_INNERERROR);
                restJson.put("data", "HttpInnerError!");
                return restJson;
            }
        };
        String result = authRoa.authToken(mockInstance, mockResInstance);

        assertEquals("{\"Information\": \"HttpInnerError!\"}", result);
    }

    @Test
    public void testAuthToken() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                JSONObject subJsonObject = new JSONObject();
                return (T)subJsonObject;
            }
        };
        new MockUp<AuthMgr>() {

            @Mock
            public JSONObject authToken(JSONObject params) {
                JSONObject restJson = new JSONObject();
                restJson.put("retCode", Constant.REST_SUCCESS);
                JSONObject data = new JSONObject();
                data.put("accessSession", "accessSession");
                data.put("userName", "userName");
                data.put("roaRand", "roaRand");
                restJson.put("data", data);
                return restJson;
            }
        };
        String result = authRoa.authToken(mockInstance, mockResInstance);

        assertEquals(
                "{\"token\": {\"methods\": [\"password\"],\"expires_at\": \"\",\"user\": {\"id\": \"userName\",\"name\": \"userName\"},\"roa_rand\": \"roaRand\"}}",
                result);
    }

    @Test
    public void testDelAuthToken() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();
        String result = authRoa.delAuthToken(mockInstance, null, null, mockResInstance);

        JSONObject resultJson = new JSONObject();
        resultJson.put("Information", "Operation success");
        assertEquals(resultJson.toString(), result);
    }

    @Test
    public void testShakehand() {
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();
        String result = authRoa.shakehand(mockInstance, null, mockResInstance);

        JSONObject resultJson = new JSONObject();
        resultJson.put("status", "running");
        resultJson.put("description", "Operation success");
        assertEquals(resultJson.toString(), result);
    }
}
