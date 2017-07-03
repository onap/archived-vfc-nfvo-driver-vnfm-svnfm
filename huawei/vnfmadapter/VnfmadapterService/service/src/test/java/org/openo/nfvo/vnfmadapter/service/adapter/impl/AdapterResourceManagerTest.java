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

package org.openo.nfvo.vnfmadapter.service.adapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openo.baseservice.roa.util.restclient.RestfulResponse;
import org.openo.nfvo.vnfmadapter.common.DownloadCsarManager;
import org.openo.nfvo.vnfmadapter.common.servicetoken.VNFRestfulUtil;
import org.openo.nfvo.vnfmadapter.testutils.JsonUtil;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class AdapterResourceManagerTest {

    AdapterResourceManager manager = null;

    @Before
    public void setUp() {
        manager = new AdapterResourceManager();
    }

    @Test(expected = JSONException.class)
    public void uploadVNFPackageTestJsonException() {
        JSONObject vnfpkg = new JSONObject();
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res != null);

    }

    @Test(expected = JSONException.class)
    public void uploadVNFPackageTestJsonException2() {
        JSONObject vnfpkg = new JSONObject();
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(null, paramsMap);
        assertTrue(res != null);

    }

    @Test
    public void uploadVNFPackageTestJsonException3() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("csarid and vnfmid are null."));

    }

    @Test
    public void uploadVNFPackageTestEmptyParam() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("csarid and vnfmid are null."));

    }

    @Test
    public void uploadVNFPackageTestNullParam() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(vnfpkg, null);
        assertTrue(res.get("reason").equals("csarid and vnfmid are null."));

    }

    @Test
    public void uploadVNFPackageTestInvalidCsrid() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("csarid is null."));

    }

    @Test
    public void uploadVNFPackageTestInvalidCsrid2() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", null);
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("csarid is null."));

    }

    @Test
    public void uploadVNFPackageTestInvalidVnfmid() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", null);
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("vnfmid is null."));

    }

    @Test
    public void uploadVNFPackageTestInvalidVnfmid2() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("vnfmid is null."));

    }

    @Test
    public void uploadVNFPackageTestNullResp() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }

    @Test
    public void downloadCsarTestNullUrl() {
        String url = null;
        String filePath = null;
        JSONObject res = manager.downloadCsar(url, filePath);
        assertTrue(res.get("reason").equals("url is null."));
    }

    @Test
    public void downloadCsarTestEmptyUrl() {
        String url = "";
        String filePath = null;
        JSONObject res = manager.downloadCsar(url, filePath);
        assertTrue(res.get("reason").equals("url is null."));
    }

    @Test
    public void downloadCsarTestNullFilePath() {
        String url = "http://localhost:8080";
        String filePath = null;
        JSONObject res = manager.downloadCsar(url, filePath);
        assertTrue(res.get("reason").equals("downloadUrl filePath is null."));
    }

    @Test
    public void downloadCsarTestEmptyFilePath() {
        String url = "http://localhost:8080";
        String filePath = "";
        JSONObject res = manager.downloadCsar(url, filePath);
        assertTrue(res.get("reason").equals("downloadUrl filePath is null."));
    }

    @Test
    public void getVnfmConnInfoTestSuccess() {
        new MockUp<VNFRestfulUtil>() {

            @Mock
            public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
                RestfulResponse resp = new RestfulResponse();
                resp.setStatus(200);
                Map<String, String> objMap = new HashMap<String, String>();
                objMap.put("id", "test123");

                String responseString = toJson(objMap);
                resp.setResponseJson(responseString);
                return resp;
            }

        };
        Map<String, String> paramsMap = new HashMap<String, String>();
        JSONObject res = manager.getVnfmConnInfo(paramsMap);
        assertTrue(res.get("id").equals("test123"));
    }

    @Test
    public void getVnfmConnInfoTestNullResp() {
        new MockUp<VNFRestfulUtil>() {

            @Mock
            public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {

                return null;
            }

        };
        Map<String, String> paramsMap = new HashMap<String, String>();
        JSONObject res = manager.getVnfmConnInfo(paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));
    }

    @Test
    public void getVnfmConnInfoTestServerError() {
        new MockUp<VNFRestfulUtil>() {

            @Mock
            public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
                RestfulResponse resp = new RestfulResponse();
                resp.setStatus(500);
                Map<String, String> objMap = new HashMap<String, String>();
                objMap.put("id", "test123");

                String responseString = toJson(objMap);
                resp.setResponseJson(responseString);
                return resp;
            }

        };
        Map<String, String> paramsMap = new HashMap<String, String>();
        JSONObject res = manager.getVnfmConnInfo(paramsMap);
        assertTrue(res.get("reason").equals("ESR return fail."));
    }

    @Test
    public void uploadTestInternalError() {
        JSONObject vnfpackage = new JSONObject();
        String vnfmurl = "http://localhost";
        String conntoken = "test";
        JSONObject res = manager.upload(vnfpackage, vnfmurl, conntoken);
        assertEquals(res.get("retCode"), 500);
    }

    public static String toJson(Map o) {
        try {
            return JsonUtil.marshal(o);
        } catch(IOException e) {
            return "";
        }
    }

    @Test
    public void testGetAllCloud(){
        String url = null;
        manager.getAllCloud(url,null);
    }

    @Test
    public void testGetAllCloud2(){
        String url = "http://127.0.0.1:31943";
        manager.getAllCloud(url,null);
    }
    @Test
    public void testUnzipCSAR(){
        manager.unzipCSAR(null,null);
    }
    @Test
    public void testUnzipCSAR2(){
        manager.unzipCSAR("vCpe.zip",null);
    }
    @Test
    public void testUnzipCSAR3(){
        manager.unzipCSAR("vCpe.zip","/opt");
    }
    @Test
    public void testUnzipCSAR4(){
        new MockUp<DownloadCsarManager>(){
            @Mock
            public  int unzipCSAR(String fileName,String filePath){
                return 0;
            }
        };
        manager.unzipCSAR("vCpe.zip","/opt");
    }
    @Test
    public void testUnzipCSAR5(){
        new MockUp<DownloadCsarManager>(){
            @Mock
            public  int unzipCSAR(String fileName,String filePath){
                return -1;
            }
        };
        manager.unzipCSAR("vCpe.zip","/opt");
    }

    @Test
    public void testGetVnfdVersion(){
        manager.getVnfdVersion("http://","127.0.0.1","token");
    }

}
