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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken.VNFRestfulUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.testutils.JsonUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulResponse;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;

/**
 * <br/>
 * <p>
 * </p>
 *
 * @author
 * @version VFC 1.0 Aug 10, 2016
 */
public class VNFRestfulUtilTest {

    @Test
    public void getRestResByDefaultTestGet(){
        String path="http://localhost:8080";
        String methodNames = "get";
        JSONObject bodyParam = new JSONObject();
        bodyParam.put("id", "1234");
        RestfulResponse resp = VNFRestfulUtil.getRestResByDefault(path, methodNames, bodyParam);
        assertNotNull(resp);
    }

    @Test
    public void getRestResByDefaultTestDelete(){
        String path="http://localhost:8080";
        String methodNames = "delete";
        JSONObject bodyParam = new JSONObject();
        bodyParam.put("id", "1234");
        RestfulResponse resp = VNFRestfulUtil.getRestResByDefault(path, methodNames, bodyParam);
        assertNotNull(resp);
    }
    @Test
    public void getRestResByDefaultTestPost(){
        String path="http://localhost:8080";
        String methodNames = "post";
        JSONObject bodyParam = new JSONObject();
        bodyParam.put("id", "1234");
        RestfulResponse resp = VNFRestfulUtil.getRestResByDefault(path, methodNames, bodyParam);
        assertNotNull(resp);
    }

    @Test
    public void sendReqToAppTestNullResp(){
        String path="http://localhost:8080";
        String methodNames = "get";
        JSONObject bodyParam = new JSONObject();
        bodyParam.put("id", "1234");
        JSONObject resp = VNFRestfulUtil.sendReqToApp(path, methodNames, bodyParam);
        assertNotNull(resp);
    }
    @Test
    public void sendReqToAppTest(){
//        new MockUp<VNFRestfulUtil>(){
//            @Mock
//            public RestfulResponse getRestResByDefault(String path, String methodNames, JSONObject bodyParam) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(200);
//                Map<String,Object> map = new HashMap<>();
//                map.put("retCode", 1);
//                resp.setResponseJson(toJson(map));
//                return resp;
//            }
//        };
        String path="http://localhost:8080/vnfdmgr/v1";
        String methodNames = "get";
        JSONObject bodyParam = new JSONObject();
        bodyParam.put("vnfmInfo", new JSONObject().put("id", "6775"));
        JSONObject resp = VNFRestfulUtil.sendReqToApp(path, methodNames, bodyParam);
        assertNotNull(resp);
    }

    @Test
    public void sendReqToAppTest2(){
//        new MockUp<VNFRestfulUtil>(){
//            @Mock
//            public RestfulResponse getRestResByDefault(String path, String methodNames, JSONObject bodyParam) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(200);
//                Map<String,Object> map = new HashMap<>();
//                map.put("retCode", -1);
//                resp.setResponseJson(toJson(map));
//                return resp;
//            }
//        };
        String path="http://localhost:8080/vnfdmgr/v1";
        String methodNames = "get";
        JSONObject bodyParam = new JSONObject();
        bodyParam.put("vnfmInfo", new JSONObject().put("id", "6775"));
        JSONObject resp = VNFRestfulUtil.sendReqToApp(path, methodNames, bodyParam);
        assertNotNull(resp);
    }
    @Test
    public void sendReqToAppTest3(){
//        new MockUp<VNFRestfulUtil>(){
//            @Mock
//            public RestfulResponse getRestResByDefault(String path, String methodNames, JSONObject bodyParam) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(500);
//                Map<String,Object> map = new HashMap<>();
//                map.put("retCode", -1);
//                resp.setResponseJson(toJson(map));
//                return resp;
//            }
//        };
        String path="http://localhost:8080/vnfdmgr/v1";
        String methodNames = "get";
        JSONObject bodyParam = new JSONObject();
        bodyParam.put("vnfmInfo", new JSONObject().put("id", "6775"));
        JSONObject resp = VNFRestfulUtil.sendReqToApp(path, methodNames, bodyParam);
        assertNotNull(resp);
    }

    @Test
    public void getRemoteResponseTest(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "delete");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        boolean isNfvoApp = false;
        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }

    @Test
    public void getRemoteResponse2Test(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "get");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        boolean isNfvoApp = false;
        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }

    @Test
    public void getRemoteResponse3Test(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "post");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        boolean isNfvoApp = false;
        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }
    @Test
    public void getRemoteResponse4Test(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "put");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        boolean isNfvoApp = false;
        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }
    @Test
    public void getRemoteResponse5Test(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "patch");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        boolean isNfvoApp = false;
        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }
    @Test
    public void getRemoteResponseTrueTest(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "patch");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        boolean isNfvoApp = true;
        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "", "test123", isNfvoApp);
        assertNull(resp);
    }

    @Test
    public void getRemoteResponseDeleteTest(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "delete");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");
        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponseGetTest(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "get");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponsePostTest(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "post");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponsePutTest(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "put");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "");
        assertNull(resp);
    }
    @Test
    public void getRemoteResponsePatchTest(){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("url", "/test/123");
        paramsMap.put("methodType", "patch");
        paramsMap.put("path", "http://localhost:8080");
        paramsMap.put("authMode", "test");

        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(paramsMap, "");
        assertNull(resp);
    }

    @Test
    public void getRemoteResponseNullTest(){

        RestfulResponse resp = VNFRestfulUtil.getRemoteResponse(null, "");
        assertNull(resp);
    }
    @Test
    public void generateParamsMapTest(){
        String url = "/test/123";
        String methodType="get";
        String path="http://localhost:8080";
        String authMode="test";
        Map<String, String> res = VNFRestfulUtil.generateParamsMap(url, methodType, path, authMode);
        assertTrue(res.get("url").equals("/test/123"));
    }

    @Test
    public void generateParams2MapTest(){
        String url = "/test/123";
        String methodType="get";
        String path="http://localhost:8080";
        Map<String, String> res = VNFRestfulUtil.generateParamsMap(url, methodType, path);
        assertTrue(res.get("url").equals("/test/123"));
    }
    @Test
    public void getResultToVnfmTest(){
        JSONObject vnfmInfo= new JSONObject();
        vnfmInfo.put("retCode", 1);
        String vnfmId="123";
        JSONObject res = VNFRestfulUtil.getResultToVnfm(vnfmInfo, vnfmId);
        assertNotNull(res);
    }

    @Test
    public void getResultToVnfm2Test(){
        JSONObject vnfmInfo= new JSONObject();
        vnfmInfo.put("retCode", -1);
        String vnfmId="123";
        JSONObject res = VNFRestfulUtil.getResultToVnfm(vnfmInfo, vnfmId);
        assertNotNull(res);
    }

    public static String toJson(Map o) {
        try {
            return JsonUtil.marshal(o);
        } catch (IOException e) {
            return "";
        }
    }
}
