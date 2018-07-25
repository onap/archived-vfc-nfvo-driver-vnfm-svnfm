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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmJsonUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.ServiceException;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process.VnfMgr;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class VnfRoaTest {

    private VnfRoa vnfRoa;

    private VnfMgr vnfMgr;

    @Before
    public void setUp() {
        vnfRoa = new VnfRoa();
        vnfMgr = new VnfMgr();
        vnfRoa.setVnfMgr(vnfMgr);
    }

    @After
    public void tearDown() {
        vnfRoa = null;
        vnfMgr = null;
    }

    @Test
    public void testAddVnf() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject addVnf(JSONObject subJsonObject, String vnfmId) {
                JSONObject retJson = new JSONObject();
                retJson.put("id", "123");
                restJson.put("data", retJson);
                return restJson;
            }
        };
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.addVnf(mockInstance, mockResInstance, "vnfmId");

        JSONObject retJson = new JSONObject();
        retJson.put("id", "123");
        assertEquals(retJson.toString(), result);

    }

    @Test
    public void testAddVnfFail() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject addVnf(JSONObject subJsonObject, String vnfmId) {
                return restJson;
            }
        };
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.addVnf(mockInstance, mockResInstance, "vnfmId");

        assertEquals(restJson.toString(), result);

    }

    @Test
    public void testAddVnfBySubJsonObjectNull() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest context) {
                return null;
            }
        };

        String result = vnfRoa.addVnf(mockInstance, mockResInstance, "vnfmId");

        assertEquals(restJson.toString(), result);

    }

    @Test
    public void testDelVnf() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject deleteVnf(String vnfId, String vnfmId, JSONObject vnfObject) {
                JSONObject retJson = new JSONObject();
                retJson.put("id", "123");
                restJson.put("data", retJson);
                return restJson;
            }
        };

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.delVnf("vnfmId", mockResInstance, "vnfId", mockInstance);
        JSONObject retJson = new JSONObject();
        retJson.put("id", "123");
        assertEquals(retJson.toString(), result);
    }

    @Test
    public void testDelVnfByVnfIdIsEmpty() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.delVnf("vnfmId", mockResInstance, "", mockInstance);

        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testDelVnfByVnfmIdIsEmpty() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.delVnf("", mockResInstance, "vnfId", mockInstance);

        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testDelVnfByVnfIdVnfmIdEmpty() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.delVnf("", mockResInstance, "", mockInstance);

        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testDelVnfFail() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject deleteVnf(String vnfId, String vnfmId, JSONObject vnfObject) {
                return restJson;
            }
        };

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.delVnf("vnfmId", mockResInstance, "vnfId", mockInstance);
        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGetVnfByVnfIdIsEmpty() throws IOException, ServiceException {
        final JSONObject restJson = new JSONObject();
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.getVnf("vnfmId", mockResInstance, "");

        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGetVnfByVnfmIdIsEmpty() throws IOException, ServiceException {
        final JSONObject restJson = new JSONObject();
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.getVnf("", mockResInstance, "vnfId");

        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGetVnfFail() throws IOException, ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject getVnf(String vnfId, String vnfmId) {
                return restJson;
            }
        };

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.getVnf("vnfmId", mockResInstance, "vnfId");
        assertEquals(restJson.toString(), result);
    }

    @Test
    public void testGetVnf() throws IOException, ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject getVnf(String vnfId, String vnfmId) {
                JSONObject retJson = new JSONObject();
                JSONObject basicInfoJson = new JSONObject();
                basicInfoJson.put("vnfInstanceId", "123");
                basicInfoJson.put("vnfInstanceName", "1");
                basicInfoJson.put("vnfInstanceDescription", "vFW");
                basicInfoJson.put("vnfdId", "1");
                basicInfoJson.put("vnfdPackageId", "vFW");
                basicInfoJson.put("version", "vFW");
                basicInfoJson.put("vnfProvider", "hw");
                basicInfoJson.put("vnfType", "fw");
                basicInfoJson.put("vnfStatus", "active");
                retJson.put("vnfInfo", basicInfoJson);
                retJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
                return retJson;
            }
        };

        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.getVnf("vnfmId", mockResInstance, "vnfId");
        JSONObject basicInfoJson = new JSONObject();
        JSONObject retJson = new JSONObject();
        basicInfoJson.put("vnfInstanceId", "123");
        basicInfoJson.put("vnfInstanceName", "1");
        basicInfoJson.put("vnfInstanceDescription", "vFW");
        basicInfoJson.put("vnfdId", "1");
        basicInfoJson.put("vnfdPackageId", "vFW");
        basicInfoJson.put("version", "vFW");
        basicInfoJson.put("vnfProvider", "hw");
        basicInfoJson.put("vnfType", "fw");
        basicInfoJson.put("vnfStatus", "active");
        retJson.put("vnfInfo", basicInfoJson);
        assertEquals(retJson.toString(), result);
    }

    @Test
    public void testGetJobByJobIdNull() throws ServiceException {
        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        String result = vnfRoa.getJob(null, null, mockResInstance, "1111");
        assertEquals("{}", result);
    }

    @Test
    public void testGetJobByVnfmIdNull() throws ServiceException {
        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        String result = vnfRoa.getJob("123", null, mockResInstance, "1111");
        assertEquals("{}", result);
    }

    @Test
    public void testGetJobByVnfMgrFail() throws ServiceException {
        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();
        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject getJob(String jobId, String vnfmId) {
                JSONObject restJson = new JSONObject();
                restJson.put(Constant.RETCODE, Constant.REST_FAIL);
                return restJson;
            }
        };
        String result = vnfRoa.getJob("123", "1234", mockResInstance, "1111");
        assertEquals("{\"retCode\":-1}", result);
    }

    @Test
    public void testGetJob() throws ServiceException {
        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject getJob(String jobId, String vnfmId) {
                JSONObject restJson = new JSONObject();
                JSONArray data = new JSONArray();
                JSONObject obj = new JSONObject();
                obj.put("id", "11111");
                obj.put("status", "Active");
                data.add(obj);
                restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
                restJson.put("data", data);
                return restJson;
            }
        };

        String result = vnfRoa.getJob("123", "1234", null, "1111");
        assertNotNull(result);
    }

    @Test
    public void testHealVnf() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject healVnf(JSONObject jsonObject, String vnfInstanceId, String vnfmId)  {
                JSONObject retJson = new JSONObject();
                retJson.put("id", "123");
                restJson.put("data", retJson);
                return restJson;
            }
        };
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.healVnf(mockInstance, mockResInstance, "id", "id");
        assertNotNull(result);

    }

    @Test
    public void testScaleVnf() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject scaleVNF(JSONObject vnfObject, String vnfmId, String vnfInstanceId) {
                JSONObject retJson = new JSONObject();
                retJson.put("id", "123");
                restJson.put("data", retJson);
                return restJson;
            }
        };
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };

        String result = vnfRoa.scaleVnf(mockInstance, mockResInstance, "id", "id");

        JSONObject retJson = new JSONObject();
        retJson.put("id", "123");
        assertEquals(retJson.toString(), result);

    }

    @Test
    public void testScaleVnfFail() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject scaleVNF(JSONObject vnfObject, String vnfmId, String vnfInstanceId) {
                JSONObject retJson = new JSONObject();
                retJson.put("id", "123");
                restJson.put("data", retJson);
                return restJson;
            }
        };
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return (T)restJson;
            }
        };
        String result = vnfRoa.scaleVnf(mockInstance, mockResInstance, "id", "id");

        assertEquals(restJson.toString(), result);

    }

    @Test
    public void testScaleVnfFail2() throws ServiceException {
        final JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        MockUp<HttpServletRequest> proxyStub = new MockUp<HttpServletRequest>() {};
        HttpServletRequest mockInstance = proxyStub.getMockInstance();

        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject scaleVNF(JSONObject vnfObject, String vnfmId, String vnfInstanceId) {
                JSONObject retJson = new JSONObject();
                retJson.put("id", "123");
                restJson.put("data", retJson);
                return restJson;
            }
        };
        new MockUp<VnfmJsonUtil>() {

            @SuppressWarnings("unchecked")
            @Mock
            public <T> T getJsonFromContexts(HttpServletRequest VNFreq) {
                return null;
            }
        };
        String result = vnfRoa.scaleVnf(mockInstance, mockResInstance, "id", "id");

        assertNotNull(result);

    }

    @Test
    public void testGetVnfmById() throws ServiceException {
        new MockUp<VnfmUtil>() {

            @Mock
            public JSONObject getVnfmById(String vnfmId) {
                JSONObject json = new JSONObject();
                json.put("vnfm", "1234");
                return json;
            }
        };
        String result = vnfRoa.getVnfmById("1234", null);
        assertNotNull(result);
    }

    @Test
    public void testGetJobFromVnfm() throws ServiceException {
        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject getJobFromVnfm(String jobId, String vnfmId) {
                JSONObject json = new JSONObject();
                json.put("retCode", "1");
                return json;
            }

            @Mock
            public String transferToLcm(JSONObject restJson) {
                return "success";
            }
        };
        String result = vnfRoa.getJobFromVnfm("jobId", "vnfmId", null, "responseId");
        assertNotNull(result);
    }

    @Test
    public void testGetJobFromVnfmFail() throws ServiceException {

        new MockUp<VnfMgr>() {

            @Mock
            public JSONObject getJobFromVnfm(String jobId, String vnfmId) {
                JSONObject json = new JSONObject();
                json.put("retCode", "-1");
                return json;
            }

        };
        MockUp<HttpServletResponse> proxyResStub = new MockUp<HttpServletResponse>() {};
        HttpServletResponse mockResInstance = proxyResStub.getMockInstance();
        String result = vnfRoa.getJobFromVnfm("jobId", "vnfmId", mockResInstance, "responseId");
        assertNotNull(result);
    }

}
