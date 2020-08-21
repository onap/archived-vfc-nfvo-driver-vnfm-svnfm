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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulResponse;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;

public class VnfmRestfulUtilTest {
    @Test
    public void testGetRestResByDefaultByNull() {
        RestfulResponse result = VnfmRestfulUtil.getRestResByDefault("path", "methodNames", new JSONObject());
        assertNull(result);
    }

    @Test
    public void testGetRestResByDefaultByGet() {
        RestfulResponse result = VnfmRestfulUtil.getRestResByDefault("path", "get", new JSONObject());
        assertNotNull(result);
    }

    @Test
    public void testGetRestResByDefaultByPut() {
        RestfulResponse result = VnfmRestfulUtil.getRestResByDefault("path", "put", new JSONObject());
        assertNotNull(result);
    }

    @Test
    public void testSendReqToApp() {
//        new MockUp<VnfmRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRestResByDefault(String path, String methodNames, JSONObject bodyParam) {
//                RestfulResponse restfulResponse = new RestfulResponse();
//                restfulResponse.setStatus(Constant.HTTP_OK);
//                String responseString = "{\"retCode\":1,\"data\":\"success\"}";
//                restfulResponse.setResponseJson(responseString);
//                return restfulResponse;
//            }
//        };
        JSONObject result = VnfmRestfulUtil.sendReqToApp("path", "put", new JSONObject());
        assertEquals(Constant.REST_SUCCESS, result.get("retCode"));
    }

    @Test
    public void testSendReqToAppByErrorMsg() {
//        new MockUp<VnfmRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRestResByDefault(String path, String methodNames, JSONObject bodyParam) {
//                RestfulResponse restfulResponse = new RestfulResponse();
//                restfulResponse.setStatus(Constant.HTTP_OK);
//                String responseString = "{\"retCode\":-1,\"data\":\"fail\",\"msg\":\"fail\"}";
//                restfulResponse.setResponseJson(responseString);
//                return restfulResponse;
//            }
//        };
        JSONObject result = VnfmRestfulUtil.sendReqToApp("path", "put", new JSONObject());
        assertEquals(Constant.REST_FAIL, result.get("retCode"));
    }

    @Test
    public void testSendReqToAppByError() {
//        new MockUp<VnfmRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRestResByDefault(String path, String methodNames, JSONObject bodyParam) {
//                RestfulResponse restfulResponse = new RestfulResponse();
//                restfulResponse.setStatus(Constant.HTTP_OK);
//                String responseString = "{\"retCode\":-1,\"data\":\"fail\"}";
//                restfulResponse.setResponseJson(responseString);
//                return restfulResponse;
//            }
//        };
        JSONObject result = VnfmRestfulUtil.sendReqToApp("path", "put", new JSONObject());
        assertEquals(Constant.REST_FAIL, result.get("retCode"));
    }

    @Test
    public void testSendReqToAppByFail() {
        JSONObject result = VnfmRestfulUtil.sendReqToApp("path", "put", new JSONObject());
        assertEquals(Constant.REST_FAIL, result.get("retCode"));
    }

    @Test
    public void testSendReqToAppByVnfmInfo() {
        JSONObject paraJson = new JSONObject();
        JSONObject vnfmObj = new JSONObject();
        vnfmObj.put("id", "id");
        paraJson.put("vnfmInfo", vnfmObj);
        JSONObject result = VnfmRestfulUtil.sendReqToApp("path", "put", paraJson);
        assertEquals(Constant.REST_FAIL, result.get("retCode"));
    }

    @Test
    public void testGenerateParamsMap2() {
        Map<String, String> result = VnfmRestfulUtil.generateParamsMap("url", "methodType", "path", "authMode");
        Map<String, String> paramsMap = new HashMap<String, String>(6);
        paramsMap.put("url", "url");
        paramsMap.put("methodType", "methodType");
        paramsMap.put("path", "path");
        paramsMap.put("authMode", "authMode");
        assertEquals(paramsMap, result);
    }
    @Test
    public void getRemoteResponseTestGet(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "get");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        boolean isNfvoApp = false;
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }
    @Test
    public void getRemoteResponseTestGetTrueNfvo(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "get");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        boolean isNfvoApp = true;
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }
    @Test
    public void getRemoteResponseTestPost(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "post");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        boolean isNfvoApp = false;
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }
    @Test
    public void getRemoteResponseTestPut(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "put");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        boolean isNfvoApp = false;
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }

    @Test
    public void getRemoteResponseTestDelete(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "delete");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        boolean isNfvoApp = false;
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }

    @Test
    public void getRemoteResponse2TestDelete(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "delete");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse("/test/123", "", "test123");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponse2TestGet(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "get");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse("/test/123", "", "test123");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponse2Testput(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "put");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse("/test/123", "", "test123");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponse2TestPost(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "put");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "post");
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse("/test/123", "", "test123");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponse2TestPatch(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "patch");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "post");
        RestfulResponse resp = VnfmRestfulUtil.getRemoteResponse("/test/123", "", "test123");
        assertNull(resp);
    }
    
    @Test
    public void getRemoteResponse() {
    	Map <String, String > headerMap = new HashMap<String, String>();
    	
    	VnfmRestfulUtil.getRemoteResponse( "url",  "get",  headerMap,"params");
    	VnfmRestfulUtil.getRemoteResponse( "url",  "post",  headerMap,"params");
    	VnfmRestfulUtil.getRemoteResponse( "url",  "put",  headerMap,"params");
    	assertNull(VnfmRestfulUtil.getRemoteResponse( "url",  "delete",  headerMap,"params"));



    }
}
