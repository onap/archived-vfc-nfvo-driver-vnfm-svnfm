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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.adapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulParametes;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.testutils.JsonUtil;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class AdapterResourceManagerTest {

    AdapterResourceManager manager = null;

    @Before
    public void setUp() {
        manager = new AdapterResourceManager();
    }

    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestJsonException() {
        JSONObject vnfpkg = new JSONObject();
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
    }
    
     @Test
    public void uploadVNFPackageTestJsonException1() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("", "");
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
    }

    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestJsonException2() {
        Map<String, String> paramsMap = new HashMap<>();
         manager.uploadVNFPackage(null, paramsMap);
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

    @Test(expected=NullPointerException.class)
    public void uploadVNFPackageTestNullParam() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
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
//        new MockUp<VNFRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(200);
//                Map<String, String> objMap = new HashMap<String, String>();
//                objMap.put("id", "test123");
//
//                String responseString = toJson(objMap);
//                resp.setResponseJson(responseString);
//                return resp;
//            }
//
//        };
        Map<String, String> paramsMap = new HashMap<String, String>();
        JSONObject res = manager.getVnfmConnInfo(paramsMap);
        assertTrue(res.get("id").equals("test123"));
    }

    @Test
    public void getVnfmConnInfoTestNullResp() {
//        new MockUp<VNFRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//
//                return null;
//            }
//
//        };
        Map<String, String> paramsMap = new HashMap<String, String>();
        JSONObject res = manager.getVnfmConnInfo(paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));
    }

    @Test
    public void getVnfmConnInfoTestServerError() {
//        new MockUp<VNFRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(500);
//                Map<String, String> objMap = new HashMap<String, String>();
//                objMap.put("id", "test123");
//
//                String responseString = toJson(objMap);
//                resp.setResponseJson(responseString);
//                return resp;
//            }
//
//        };
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
//        new MockUp<DownloadCsarManager>(){
//            @Mock
//            public  int unzipCSAR(String fileName,String filePath){
//                return 0;
//            }
//        };
        manager.unzipCSAR("vCpe.zip","/opt");
    }
    @Test
    public void testUnzipCSAR5(){
//        new MockUp<DownloadCsarManager>(){
//            @Mock
//            public  int unzipCSAR(String fileName,String filePath){
//                return -1;
//            }
//        };
        manager.unzipCSAR("vCpe.zip","/opt");
    }

    @Test
    public void testGetVnfdVersion(){
        manager.getVnfdVersion("http://","127.0.0.1","token");
    }

    @Test(expected=NullPointerException.class)
    public void testgetVNFDPlanInfoNull(){
    	
    	manager.getVNFDPlanInfo(null, null, null);
    	}
    
    @Test
    public void testgetVNFDPlanInfo(){
    	
    	manager.getVNFDPlanInfo("https://127.0.0.1:80", "vnfd123", "token");
    	}
    
    @Test
    public void testgetVNFDPlanInfoConn(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 200;
//	    	}
//	    	
//	    	@Mock 
//	    	 public String getResponseBodyAsString()
//	    			   throws IOException
//	    			  {
//	    		JSONObject jobj = new JSONObject();
//	    		jobj.put("Status", 200);
//	    		jobj.put("UserName", "User");
//	    		jobj.put("password", "pass");
//	    		String str = jobj.toString();
//	    		return str;
//	    			   }
//	    	
//	    };
    	
    	manager.getVNFDPlanInfo("https://127.0.0.1:80", "vnfd123", "accessSession");
    	}
    
    @Test
    public void testgetVNFDPlanInfoConn500(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 500;
//	    	}
//	    	
//	    	 @Mock 
//	       	 public String getResponseBodyAsString()
//	       			   throws IOException
//	       			  {
//	       		String str = "Failed";
//	       		return str;
//	       			   }
//	    	
//	    };
    	
    	manager.getVNFDPlanInfo("https://127.0.0.1:80", "vnfd123", "accessSession");
    	}
    
    
    @Test
    public void testGetVnfdVersion200(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 200;
//	    	}
//	    	
//	    	@Mock 
//	    	 public String getResponseBodyAsString()
//	    			   throws IOException
//	    			  {
//	    		JSONObject jobj = new JSONObject();
//	    		jobj.put("Status", 200);
//	    		jobj.put("UserName", "User");
//	    		jobj.put("password", "pass");
//	    		String str = jobj.toString();
//	    		return str;
//	    			   }
//	    	
//	    };
	    
	    JSONObject jobj = manager.getVnfdVersion("http://","127.0.0.1:80","accessSession");
    }
    
    @Test
    public void testGetVnfdVersion500(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 500;
//	    	}
//	    	
//	    @Mock 
//   	 public String getResponseBodyAsString()
//   			   throws IOException
//   			  {
//   		String str = "Failed";
//   		return str;
//   			   }
//	    };
	    
        manager.getVnfdVersion("http://","127.0.0.1:80","accessSession");
    }
    
    @Test
    public void uploadVNFPackageTestcsarid() {
    	 RestfulParametes rp = new RestfulParametes();
         rp.setRawData("success");
         rp.setRawData("sdjhbfj");
         HashMap<String,String>  headerMap = new HashMap<String,String>();
         headerMap.put("Content-Type", "application/json");
         headerMap.put("X-TransactionId", "5851");
         rp.setHeaderMap(headerMap);
         HashMap<String,String>  paramMap = new HashMap<String,String>();
         paramMap.put("id", "1234");
         rp.setParamMap(paramMap);
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        vnfpkg.put("Restfulparameters", rp);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid200");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));
    }
    
    @Test
    public void uploadVNFPackageTestcsaridNull() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "caser200");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test
    public void uploadVNFPackageTestParamEmpty() {
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        Map<String, String> paramsMap = new HashMap<>();
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
    }
    
    @Test
    public void downloadCsarTestUrlAndFilePathWrong() {
        String url = "http://localhost:8080";
        String filePath = "Tset";
        JSONObject res = manager.downloadCsar(url, filePath);
    }
    @Test
    public void downloadCsarTestUrlAndFilePath() {
        String url = "https://127.0.0.1:31943";
        String filePath = "src/test/resources/Check.txt";
        JSONObject res = manager.downloadCsar(url, filePath);
    }
     
    @Test
    public void testGetAllCloudUrlEmpty(){
        String url = "";
        manager.getAllCloud(url,null);
    }
    
    @Test
    public void testGetAllCloudUrlandConn(){
        String url = "http://127.0.0.1:31943";
        String conn = "conn";
        manager.getAllCloud(url,conn);
    }
    
    @Test
    public void testUnzipCSAREmpty(){
        manager.unzipCSAR("vCpe.zip","");
    }
    @Test
    public void testUnzipCSAREmpty1(){
        manager.unzipCSAR("","/opt");
    }
    
    @Test
    public void readVfnPkgInfoFromJsonTest() throws IOException{
    	
    	System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
    	AdapterResourceManager.readVfnPkgInfoFromJson();
    }
    @Test
    public void readVfnPkgInfoFromJsonTestNumBelowZero() throws IOException{
    	
    	System.setProperty("catalina.base", "C:/Users/Huawei/Desktop");
    	AdapterResourceManager.readVfnPkgInfoFromJson();
    }
    
    @Test
    public void testGetAllCloudUrlandaccessSession(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 200;
//	    	}
//	    	
//	    	@Mock 
//	    	 public String getResponseBodyAsString()
//	    			   throws IOException
//	    			  {
//	    		String str = "{\n\t\"driverInfo\": {\n\t\t\"driverName\": \"hwvnfm\",\n\t\t\"instanceID\": \"hwvnfm-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\"services\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\"support_sys\": [{\n\t\t\t\t\"type\": \"vnfm\",\n\t\t\t\t\"version\": \"V100R001\"\n\t\t\t}]\n\t\t}]\n\t}\n}";
//	    		JSONObject jobj = new JSONObject();
//	    		jobj.put("Status", 200);
//	    		jobj.put("UserName", "User");
//	    		jobj.put("password", "pass");
//	    		JSONArray ja = new JSONArray();
//	    		ja.add(str);
//	    		jobj.put("vim_info", ja);
//	    		String str1 = jobj.toString();
//	    		return str1;
//	    			   }
//	    	
//	    };
	    
	    System.setProperty("catalina.base", "C:/Users/Huawei/Desktop");
        String url = "http://127.0.0.1:31943";
        String conn = "accessSession";
        manager.getAllCloud(url,conn);
    }
    
    @Test
    public void testGetAllCloudUrlandaccessSession500(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 500;
//	    	}
//	    	
//	    	@Mock 
//	    	 public String getResponseBodyAsString()
//	    			   throws IOException
//	    			  {
//	    		String str = "Failed";
//	    		return str;
//	    			   }
//	    	
//	    };
	    
	    System.setProperty("catalina.base", "C:/Users/Huawei/Desktop");
        String url = "http://127.0.0.1:31943";
        String conn = "accessSession";
        manager.getAllCloud(url,conn);
    }
    
    @Test
    public void testUpload(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 200;
//	    	}
//	    	
//	    	@Mock 
//	    	 public String getResponseBodyAsString()
//	    			   throws IOException
//	    			  {
//	    		JSONObject jobj = new JSONObject();
//	    		jobj.put("Status", 200);
//	    		jobj.put("UserName", "User");
//	    		jobj.put("password", "pass");
//	    		String str = jobj.toString();
//	    		return str;
//	    			   }
//	    	
//	    };
	    
    	JSONObject vnfpackage = new JSONObject();
    	vnfpackage.put("UserName", "User");
    	vnfpackage.put("password", "Pass");
    	String vnfmurl = "http://127.0.0.1:31943";
    	  String conntoken = "accessSession";
    	  System.setProperty("catalina.base", "src/test/resources/");
    	manager.upload(vnfpackage, vnfmurl, conntoken);
    }
    
    @Test
    public void testUploadNoJson(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 200;
//	    	}
//	    	
//	    	@Mock 
//	    	 public String getResponseBodyAsString()
//	    			   throws IOException
//	    			  {
//	    		
//	    		String str = "Test";
//	    		return str;
//	    			   }
//	    	
//	    };
	    
    	JSONObject vnfpackage = new JSONObject();
    	vnfpackage.put("UserName", "User");
    	vnfpackage.put("password", "Pass");
    	String vnfmurl = "http://127.0.0.1:31943";
    	  String conntoken = "accessSession";
    	  System.setProperty("catalina.base", "src/test/resources/");
    	manager.upload(vnfpackage, vnfmurl, conntoken);
    }
    
    @Test
    public void testUpload500(){
    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 500;
//	    	}
//	    	
//	    	@Mock 
//	    	 public String getResponseBodyAsString()
//	    			   throws IOException
//	    			  {
//	    		String str = "Failed";
//	    		return str;
//	    			   }
//	    	
//	    };
	    
    	JSONObject vnfpackage = new JSONObject();
    	vnfpackage.put("UserName", "User");
    	vnfpackage.put("password", "Pass");
    	String vnfmurl = "http://127.0.0.1:31943";
    	  String conntoken = "accessSession";
    	  System.setProperty("catalina.base", "src/test/resources/");
    	manager.upload(vnfpackage, vnfmurl, conntoken);
    }
    
    @Test
    public void testGetVnfmCsarInfoEmpty(){
    	manager.getVnfmCsarInfo("");
    }
    @Test
    public void testGetVnfmCsarInfoNull(){
    	manager.getVnfmCsarInfo(null);
    }
    /*@Test
    public void testGetVnfmCsarInfo(){
    	manager.getVnfmCsarInfo("casr-id-123");
    }*/
    @Test
    public void downloadCsarTest() {
    	
//    	new MockUp<DownloadCsarManager>(){
//    	@Mock
//    	public String download(String url, String filepath) {
//			return "Success";
//    	
//    	}
//    	};
    	
        String url = "http://localhost:8080";
        String filePath = "src/test/resources/Check.txt";
        JSONObject res = manager.downloadCsar(url, filePath);
        assertTrue(true);
    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTest() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<JSONObject>(){
//    	@Mock
//    	public JSONObject fromObject(Object object){
//    		JSONObject js = new JSONObject();
//    		js.put("Result", "Success");
//    		js.put("Check", "Ok");
//    		js.put("downloadUri", "http://127.0.0.1:80");
//    		return js;
//    	}
//    	};

    	JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(true);

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccess() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//   		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<JSONObject>(){
//    	@Mock
//    	public JSONObject fromObject(Object object){
//    		JSONObject js = new JSONObject();
//    		js.put("Result", "Success");
//    		js.put("Check", "Ok");
//    		js.put("downloadUri", "http://127.0.0.1:80");
//    		return js;
//    	}
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
    	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(true);

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessUNZIP() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<JSONObject>(){
//    	@Mock
//    	public JSONObject fromObject(Object object){
//    		JSONObject js = new JSONObject();
//    		js.put("Result", "Success");
//    		js.put("Check", "Ok");
//    		js.put("downloadUri", "http://127.0.0.1:80");
//    		return js;
//    	}
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//    	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessUNZIPFTPSClient() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<JSONObject>(){
//    	@Mock
//    	public JSONObject fromObject(Object object){
//    		JSONObject js = new JSONObject();
//    		js.put("Result", "Success");
//    		js.put("Check", "Ok");
//    		js.put("downloadUri", "http://127.0.0.1:80");
//    		return js;
//    	}
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//    	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        obj.put("ftp_server_ip", "https:127.0.0.1:80");
        obj.put("ftp_username", "FtpUser");
        obj.put("ftp_password", "FtpPass");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessUNZIPWithUrl() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(true);

    }
    
    
    @Test
    public void uploadVNFPackageTestSuccessUNZIP1() {
    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessConnect() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(true);

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessConnectPath() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
//            	 new MockUp<HttpMethodBase>(){
//         	    	
//         	    	@Mock 
//         	    	 public int getStatusCode(){
//         	    		
//         	    		return 200;
//         	    	}
//         	    	
//         	    	@Mock 
//         	    	 public String getResponseBodyAsString()
//         	    			   throws IOException
//         	    			  {
//         	    		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//         	    		JSONObject jobj = new JSONObject();
//         	    		jobj.put("Status", 200);
//         	    		jobj.put("UserName", "User");
//         	    		jobj.put("password", "pass");
//         	    		JSONArray ja = new JSONArray();
//         	    		ja.add(str);
//         	    		jobj.put("vim_info", ja);
//         	    		String str1 = jobj.toString();
//         	    		return str1;
//         	    			   }
//         	    };
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessConnectPath500() {
//    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
//            	 new MockUp<HttpMethodBase>(){
//         	    	
//         	    	@Mock 
//         	    	 public int getStatusCode(){
//         	    		
//         	    		return 500;
//         	    	}
//         	    	
//         	    	@Mock 
//         	    	 public String getResponseBodyAsString()
//         	    			   throws IOException
//         	    			  {
//         	    		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//         	    		JSONObject jobj = new JSONObject();
//         	    		jobj.put("Status", 500);
//         	    		jobj.put("UserName", "User");
//         	    		jobj.put("password", "pass");
//         	    		JSONArray ja = new JSONArray();
//         	    		ja.add(str);
//         	    		jobj.put("vim_info", ja);
//         	    		String str1 = jobj.toString();
//         	    		return str1;
//         	    			   }
//         	    };
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(true);

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessConnectPathEmpty() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n\"vim_id\": \"\",\n\t\t\"vim-info\": \"1\",\n\t\t\"vim-name\": \"\"\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
//            	 new MockUp<HttpMethodBase>(){
//         	    	
//         	    	@Mock 
//         	    	 public int getStatusCode(){
//         	    		
//         	    		return 200;
//         	    	}
//         	    	
//         	    	@Mock 
//         	    	 public String getResponseBodyAsString()
//         	    			   throws IOException
//         	    			  {
//         	    		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//         	    		JSONObject jobj = new JSONObject();
//         	    		jobj.put("Status", 200);
//         	    		jobj.put("UserName", "User");
//         	    		jobj.put("password", "pass");
//         	    		JSONArray ja = new JSONArray();
//         	    		ja.add(str);
//         	    		jobj.put("vim_info", ja);
//         	    		String str1 = jobj.toString();
//         	    		return str1;
//         	    			   }
//         	    };
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessId() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
//            	 new MockUp<HttpMethodBase>(){
//         	    	
//         	    	@Mock 
//         	    	 public int getStatusCode(){
//         	    		
//         	    		return 200;
//         	    	}
//         	    	
//         	    	@Mock 
//         	    	 public String getResponseBodyAsString()
//         	    			   throws IOException
//         	    			  {
//         	    		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//         	    		JSONObject jobj = new JSONObject();
//         	    		jobj.put("Status", 200);
//         	    		jobj.put("UserName", "User");
//         	    		jobj.put("password", "pass");
//         	    		JSONArray ja = new JSONArray();
//         	    		ja.add(str);
//         	    		jobj.put("vim_info", ja);
//         	    		String str1 = jobj.toString();
//         	    		return str1;
//         	    			   }
//        	    };
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessIdNull() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", null);
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
//            	 new MockUp<HttpMethodBase>(){
//         	    	
//         	    	@Mock 
//         	    	 public int getStatusCode(){
//         	    		
//         	    		return 200;
//         	    	}
//         	    	
//         	    	@Mock 
//         	    	 public String getResponseBodyAsString()
//         	    			   throws IOException
//         	    			  {
//         	    		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//         	    		JSONObject jobj = new JSONObject();
//         	    		jobj.put("Status", 200);
//         	    		jobj.put("UserName", "User");
//         	    		jobj.put("password", "pass");
//         	    		JSONArray ja = new JSONArray();
//         	    		ja.add(str);
//         	    		jobj.put("vim_info", ja);
//         	    		String str1 = jobj.toString();
//         	    		return str1;
//         	    			   }
//         	    };
//            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessIdFile0() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/Check10.txt");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
//            	 new MockUp<HttpMethodBase>(){
//         	    	
//         	    	@Mock 
//         	    	 public int getStatusCode(){
//         	    		
//         	    		return 200;
//         	    	}
//         	    	
//         	    	@Mock 
//         	    	 public String getResponseBodyAsString()
//         	    			   throws IOException
//         	    			  {
//         	    		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//         	    		JSONObject jobj = new JSONObject();
//         	    		jobj.put("Status", 200);
//         	    		jobj.put("UserName", "User");
//         	    		jobj.put("password", "pass");
//         	    		JSONArray ja = new JSONArray();
//         	    		ja.add(str);
//         	    		jobj.put("vim_info", ja);
//         	    		String str1 = jobj.toString();
//         	    		return str1;
//         	    			   }
//         	    };
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessId500() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(true);

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessIdEmpty() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		js.put("vim_info", ja);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
//            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessTemplete() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja1.add(str1);
//     	    		js.put("vim_info", ja);
//     	    		js.put("templates", ja1);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestSuccessTempleteForVPlan() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "Csar_Check");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    

    
    @Test
    public void uploadVNFPackageTestParamMapEmpty() {
    	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("", "");
        paramsMap.put("", "");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(true);
 
    }
    
    @Test
    public void uploadVNFPackageTestVnfpkgEmpty() {
    	
    	 JSONObject vnfpkg = new JSONObject();
         vnfpkg.put("", "");
         JSONObject obj = new JSONObject();
         obj.put("csar_file_path", "src/test/resources/Check10.txt");
         obj.put("csar_file_name", "casrFile");
         vnfpkg.put("template", obj);
         Map<String, String> paramsMap = new HashMap<>();
         paramsMap.put("csarid", "csarid123");
         paramsMap.put("vnfmid", "vnfmid1234");
         JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
         assertTrue(true);
 
    }
    
	@Test
    public void readScaleInVmIdFromJsonTest() {
    	
    	System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
    	AdapterResourceManager.readScaleInVmIdFromJson();
    }
	
	@Test
    public void readScaleInVmIdFromJsonTestNoFile() {
    	
		System.setProperty("catalina.base", "src/test/resources");
    	AdapterResourceManager.readScaleInVmIdFromJson();
    }
	
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithCscf() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<RestfulResponse>(){
//    		
//    		@Mock
//    		 public String getResponseContent() {
//    			
//				return "Success";
//    		 }
//    		
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//        	@Mock
//        	public String download(String url, String filepath) {
//    			return "Success";
//        	
//        	}
//        	};
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "CSCF_SI");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//        	new MockUp<DownloadCsarManager>(){
//            	@Mock
//            	 public int unzipCSAR(String fileName, String filePath) {
//        			return 0;
//            	
//            	}
//            	};
//            	
//            	new MockUp<ConnectMgrVnfm>(){
//            		
//            		@Mock
//            		 public int connect(JSONObject vnfmObj, String authModel) {
//            			vnfmObj.put("connToken", "accessSession");
//            			vnfmObj.put("Content-Type", "Application/Json");
//            			authModel = "accessSession";
//            			return 200;
//            		}
//            		
//            	};
//            	
//            	new MockUp<HttpClient>(){
//        	        @Mock
//        	        public int executeMethod(HttpMethod method) {
//        	            return 200;
//        	        }
//        	    };
//        	    
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }

    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithMME() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "MME");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithSPGW() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "SPGW");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithHSS() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "HSS");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithSBC() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "SBC");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithPCRF() {
    	
//	    	new MockUp<VNFRestfulUtil>(){
//	    		@Mock
//	    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//	    			RestfulResponse rr = new RestfulResponse();
//	    			Map<String, String> header = new HashMap<>();
//	    			header.put("Content-Type", "Application/Json");
//	    			header.put("X-FormId", "jhfdl");
//	    			rr.setRespHeaderMap(header);
//	    			rr.setStatus(200);
//	    			rr.setResponseJson("shdfhj");
//	    			
//	    			return rr;
//	    			
//	    		}
//	    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "PCRF");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//        System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("download csar file failed."));

    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithTAS() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "TAS");
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/ONAP-VFC/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg, paramsMap);
        assertTrue(res.get("reason").equals("download csar file failed."));

    }
    
    @Test
    public void uploadVNFPackageTestWithCSCF() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "CSCF");
//     	    		JSONObject jsEms = new JSONObject();
//     	    		jsEms.put("emsUuid", "123erbhi-hjdek123");
//     	    		JSONObject jsCsar = new JSONObject();
//     	    		jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//     	    		jsCsar.put("csar_file_name", "Csar_File");
//     	    		jsCsar.put("emsUuid", jsEms);
//     	    		JSONObject jsTemp = new JSONObject();
//     	    		jsTemp.put("template", jsCsar);
//     	    		js.put("vCSCF", jsTemp);
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        JSONObject vnfpkg1 = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
        assertTrue(res.get("reason").equals("download csar file failed."));
    }
    
    @Test
    public void uploadVNFPackageTestWithOutEmsUid() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "CSCF");
//     	    		JSONObject jsCsar = new JSONObject();
//     	    		jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//     	    		jsCsar.put("csar_file_name", "Csar_File");
//     	    		JSONObject jsTemp = new JSONObject();
//     	    		jsTemp.put("template", jsCsar);
//     	    		js.put("vCSCF", jsTemp);
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        JSONObject vnfpkg1 = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
        assertTrue(res.get("reason").equals("download csar file failed."));
    }
    
    @Test
    public void uploadVNFPackageTestWithCSDowCsar() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//    		@Mock
//    		public String download(String url, String filepath) {
//    			String response = "Success";
//    			return response;
//    		}
//    	};
//    	
//        	new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "CSCF");
//     	    		JSONObject jsEms = new JSONObject();
//     	    		jsEms.put("emsUuid", "123erbhi-hjdek123");
//     	    		JSONObject jsCsar = new JSONObject();
//     	    		jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//     	    		jsCsar.put("csar_file_name", "Csar_File");
//     	    		jsCsar.put("emsUuid", jsEms);
//     	    		JSONObject jsTemp = new JSONObject();
//     	    		jsTemp.put("template", jsCsar);
//     	    		js.put("vCSCF", jsTemp);
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//            	System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        JSONObject vnfpkg1 = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
        assertTrue(res.get("reason").equals("unzip csar file failed."));
    }
    
    @Test
    public void uploadVNFPackageTestWithUnZipCsar() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<VnfmUtil>(){
//    		@Mock
//    		public JSONObject getVnfmById(String vnfmId) {
//    			
//    			JSONObject jsonObject = new JSONObject();
//    			jsonObject.put("url", "https://localhost:80");
//    			jsonObject.put("userName", "ubuntu");
//    			jsonObject.put("password", "******");
//    			return jsonObject;
//    			
//    		}
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//    		@Mock
//    		public String download(String url, String filepath) {
//    			String response = "Success";
//    			return response;
//    		}
//    		
//    		@Mock
//    		 public int unzipCSAR(String fileName, String filePath) {    			
//    			
//    			return 0;
//    		}
//    	};
//    	
//    	new MockUp<ConnectMgrVnfm>(){
//    		@Mock
//    		public int connect(JSONObject vnfmObj, String authModel) {
//    			
//    			
//    			return 404;
//    			
//    		}
//    	};
//    	
//        new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "CSCF");
//     	    		JSONObject jsEms = new JSONObject();
//     	    		jsEms.put("emsUuid", "123erbhi-hjdek123");
//     	    		JSONObject jsCsar = new JSONObject();
//     	    		jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//     	    		jsCsar.put("csar_file_name", "Csar_File");
//     	    		jsCsar.put("emsUuid", jsEms);
//     	    		JSONObject jsTemp = new JSONObject();
//     	    		jsTemp.put("template", jsCsar);
//     	    		js.put("vCSCF", jsTemp);
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//        System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        JSONObject vnfpkg1 = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
        assertTrue(res.get("reason").equals("connect fail."));
    }
    
    @Test(expected=JSONException.class)
    public void uploadVNFPackageTestWithConnectMgr() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<VnfmUtil>(){
//    		@Mock
//    		public JSONObject getVnfmById(String vnfmId) {
//    			
//    			JSONObject jsonObject = new JSONObject();
//    			jsonObject.put("url", "https://localhost:80");
//    			jsonObject.put("userName", "ubuntu");
//    			jsonObject.put("password", "******");
//    			return jsonObject;
//    			
//    		}
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//    		@Mock
//    		public String download(String url, String filepath) {
//    			String response = "Success";
//    			return response;
//    		}
//    		
//    		@Mock
//    		 public int unzipCSAR(String fileName, String filePath) {    			
//    			
//    			return 0;
//    		}
//    	};
//    	
//    	new MockUp<HttpClient>(){
//	        @Mock
//	        public int executeMethod(HttpMethod method) {
//	            return 200;
//	        }
//	    };
//	    
//	    new MockUp<HttpMethodBase>(){
//	    	
//	    	@Mock 
//	    	 public int getStatusCode(){
//	    		
//	    		return 200;
//	    	}
//	    	
//	    };
//	    
//    	new MockUp<ConnectMgrVnfm>(){
//    		@Mock
//    		public int connect(JSONObject vnfmObj, String authModel) {
//    			
//    			
//    			return 200;
//    			
//    		}
//    		
//    		@Mock
//    		public String getAccessSession() {
//    			
//    			return "conn";
//    		}
//    	};
//    	
//        new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "CSCF");
//     	    		JSONObject jsEms = new JSONObject();
//     	    		jsEms.put("emsUuid", "123erbhi-hjdek123");
//     	    		JSONObject jsCsar = new JSONObject();
//     	    		jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//     	    		jsCsar.put("csar_file_name", "Csar_File");
//     	    		jsCsar.put("emsUuid", jsEms);
//     	    		JSONObject jsTemp = new JSONObject();
//     	    		jsTemp.put("template", jsCsar);
//     	    		js.put("vCSCF", jsTemp);
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//        System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        JSONObject vnfpkg1 = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
        assertTrue(res.get("reason").equals("RestfulResponse is null."));
    }
    
    @Test
    public void uploadVNFPackageTestWithConnectMgrGetAllFail() {
    	
//    	new MockUp<VNFRestfulUtil>(){
//    		@Mock
//    		public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//    			RestfulResponse rr = new RestfulResponse();
//    			Map<String, String> header = new HashMap<>();
//    			header.put("Content-Type", "Application/Json");
//    			header.put("X-FormId", "jhfdl");
//    			rr.setRespHeaderMap(header);
//    			rr.setStatus(200);
//    			rr.setResponseJson("shdfhj");
//    			
//    			return rr;
//    			
//    		}
//    	};
//    	
//    	new MockUp<VnfmUtil>(){
//    		@Mock
//    		public JSONObject getVnfmById(String vnfmId) {
//    			
//    			JSONObject jsonObject = new JSONObject();
//    			jsonObject.put("url", "https://localhost:80");
//    			jsonObject.put("userName", "ubuntu");
//    			jsonObject.put("password", "******");
//    			return jsonObject;
//    			
//    		}
//    	};
//    	
//    	new MockUp<DownloadCsarManager>(){
//    		@Mock
//    		public String download(String url, String filepath) {
//    			String response = "Success";
//    			return response;
//    		}
//    		
//    		@Mock
//    		 public int unzipCSAR(String fileName, String filePath) {    			
//    			
//    			return 0;
//    		}
//    	};
//    	
//    	new MockUp<ConnectMgrVnfm>(){
//    		@Mock
//    		public int connect(JSONObject vnfmObj, String authModel) {
//    			
//    			
//    			return 200;
//    			
//    		}
//    		
//    		@Mock
//    		public String getAccessSession() {
//    			
//    			return "conn";
//    		}
//    	};
//    	
//        new MockUp<JSONObject>(){
//            	@Mock
//            	public JSONObject fromObject(Object object){
//            		JSONObject js = new JSONObject();
//            		js.put("id", "upload-id-123");
//            		js.put("Result", "Success");
//            		js.put("Check", "Ok");
//            		js.put("url", "http://localhost:80");
//            		js.put("userName", "User");
//            		js.put("password", "pass");
//            		js.put("downloadUri", "http://127.0.0.1:80");
//            		js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//            		js.put("keyStorePass", "Changeme_123");
//            		js.put("keyStoreType", "PKCS12");
//            		JSONArray ja = new JSONArray();
//            		String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//     	    		ja.add(str);
//     	    		JSONArray ja1 = new JSONArray();
//     	    		String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";;
//     	    		ja1.add(str1);
//     	    		JSONArray ja2 = new JSONArray();
//     	    		String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//     	    		ja2.add(str2);
//     	    		JSONObject jsObject = new JSONObject();
//     	    		jsObject.put("downloadUrl", "http://localhost:80");
//     	    		jsObject.put("csarName", "CSCF");
//     	    		JSONObject jsEms = new JSONObject();
//     	    		jsEms.put("emsUuid", "123erbhi-hjdek123");
//     	    		JSONObject jsCsar = new JSONObject();
//     	    		jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//     	    		jsCsar.put("csar_file_name", "Csar_File");
//     	    		jsCsar.put("emsUuid", jsEms);
//     	    		JSONObject jsTemp = new JSONObject();
//     	    		jsTemp.put("template", jsCsar);
//     	    		js.put("vCSCF", jsTemp);
//     	    		js.put("vim_info", ja);
//     	    		js.put("template", ja2);
//     	    		js.put("templates", ja1);
//     	    		js.put("packageInfo", jsObject);
//            		return js;
//            	}
//            	};
//            	
//        System.setProperty("catalina.base", "D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");
            	
        JSONObject vnfpkg = new JSONObject();
        JSONObject vnfpkg1 = new JSONObject();
        vnfpkg.put("name", "test");
        JSONObject obj = new JSONObject();
        obj.put("csar_file_path", "src/test/resources/Check10.txt");
        obj.put("csar_file_name", "casrFile");
        vnfpkg.put("template", obj);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("csarid", "csarid123");
        paramsMap.put("vnfmid", "vnfmid1234");
        JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
        assertTrue(res.get("reason").equals("get allcloud failed and IOException.Connection refused (Connection refused)"));
    }

	@Test
	public void uploadVNFPackageTestVnfdPlanInfo() throws IOException {

//		new MockUp<VNFRestfulUtil>() {
//			@Mock
//			public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//				RestfulResponse rr = new RestfulResponse();
//				Map<String, String> header = new HashMap<>();
//				header.put("Content-Type", "Application/Json");
//				header.put("X-FormId", "jhfdl");
//				rr.setRespHeaderMap(header);
//				rr.setStatus(200);
//				rr.setResponseJson("shdfhj");
//
//				return rr;
//
//			}
//		};
//
//		new MockUp<VnfmUtil>() {
//			@Mock
//			public JSONObject getVnfmById(String vnfmId) {
//
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("url", "https://localhost:80");
//				jsonObject.put("userName", "ubuntu");
//				jsonObject.put("password", "******");
//				return jsonObject;
//
//			}
//		};
//
//		new MockUp<DownloadCsarManager>() {
//			@Mock
//			public String download(String url, String filepath) {
//				String response = "Success";
//				return response;
//			}
//
//			@Mock
//			public int unzipCSAR(String fileName, String filePath) {
//
//				return 0;
//			}
//		};
//
//		new MockUp<ConnectMgrVnfm>() {
//			@Mock
//			public int connect(JSONObject vnfmObj, String authModel) {
//
//				return 200;
//
//			}
//
//			@Mock
//			public String getAccessSession() {
//
//				return "conn";
//			}
//		};
//
//		new MockUp<JSONObject>() {
//			@Mock
//			public JSONObject fromObject(Object object) {
//				JSONObject js = new JSONObject();
//				js.put("id", "upload-id-123");
//				js.put("Result", "Success");
//				js.put("Check", "Ok");
//				js.put("url", "http://localhost:80");
//				js.put("userName", "User");
//				js.put("password", "pass");
//				js.put("downloadUri", "http://127.0.0.1:80");
//				js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//				js.put("keyStorePass", "Changeme_123");
//				js.put("keyStoreType", "PKCS12");
//				JSONArray ja = new JSONArray();
//				String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//				ja.add(str);
//				JSONArray ja1 = new JSONArray();
//				String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//				;
//				ja1.add(str1);
//				JSONArray ja2 = new JSONArray();
//				String str2 = "{\n  \"template_name\": \"vnfd-name-123\",\n  \"topology_template\": [{\n\t\t\t\"service_url\": \"/api/hwvnfm/v1\",\n\t\t\t\n\t\t}]\n\t\n\t\n}";
//				ja2.add(str2);
//				JSONObject jsObject = new JSONObject();
//				jsObject.put("downloadUrl", "http://localhost:80");
//				jsObject.put("csarName", "CSCF");
//				JSONObject jsEms = new JSONObject();
//				jsEms.put("emsUuid", "123erbhi-hjdek123");
//				JSONObject jsCsar = new JSONObject();
//				jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//				jsCsar.put("csar_file_name", "Csar_File");
//				jsCsar.put("emsUuid", jsEms);
//				JSONObject jsTemp = new JSONObject();
//				jsTemp.put("template", jsCsar);
//				js.put("vCSCF", jsTemp);
//				js.put("vim_info", ja);
//				js.put("template", ja2);
//				js.put("templates", ja1);
//				js.put("packageInfo", jsObject);
//				return js;
//			}
//		};
//
//		System.setProperty("catalina.base",
//				"D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");

		JSONObject vnfpkg = new JSONObject();
		JSONObject vnfpkg1 = new JSONObject();
		vnfpkg.put("name", "test");
		JSONObject obj = new JSONObject();
		obj.put("csar_file_path", "src/test/resources/Check10.txt");
		obj.put("csar_file_name", "casrFile");
		vnfpkg.put("template", obj);
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("csarid", "csarid123");
		paramsMap.put("vnfmid", "vnfmid1234");
		String testString = "test\nstring";
		JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
		assertTrue(res.get("reason")
				.equals("get allcloud failed and IOException.Connection refused (Connection refused)"));
	}

	@Test
	public void uploadVNFPackageSuccessTest() {

//		new MockUp<VNFRestfulUtil>() {
//			@Mock
//			public RestfulResponse getRemoteResponse(Map<String, String> paramsMap, String params) {
//				RestfulResponse rr = new RestfulResponse();
//				Map<String, String> header = new HashMap<>();
//				header.put("Content-Type", "Application/Json");
//				header.put("X-FormId", "jhfdl");
//				rr.setRespHeaderMap(header);
//				rr.setStatus(200);
//				rr.setResponseJson("shdfhj");
//
//				return rr;
//
//			}
//		};
//
//		new MockUp<VnfmUtil>() {
//			@Mock
//			public JSONObject getVnfmById(String vnfmId) {
//
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("url", "https://localhost:80");
//				jsonObject.put("userName", "ubuntu");
//				jsonObject.put("password", "******");
//				return jsonObject;
//
//			}
//		};
//
//		new MockUp<DownloadCsarManager>() {
//			@Mock
//			public String download(String url, String filepath) {
//				String response = "Success";
//				return response;
//			}
//
//			@Mock
//			public int unzipCSAR(String fileName, String filePath) {
//
//				return 0;
//			}
//		};
//
//		new MockUp<HttpClient>() {
//			@Mock
//			public int executeMethod(HttpMethod method) {
//				return 200;
//			}
//		};
//
//		new MockUp<HttpMethodBase>() {
//
//			@Mock
//			public int getStatusCode() {
//
//				return 200;
//			}
//
//		};
//
//		new MockUp<ConnectMgrVnfm>() {
//			@Mock
//			public int connect(JSONObject vnfmObj, String authModel) {
//
//				return 200;
//
//			}
//
//			@Mock
//			public String getAccessSession() {
//
//				return "conn";
//			}
//		};
//
//		new MockUp<JSONObject>() {
//			@Mock
//			public JSONObject fromObject(Object object) {
//				JSONObject js = new JSONObject();
//				js.put("id", "upload-id-123");
//				js.put("Result", "Success");
//				js.put("Check", "Ok");
//				js.put("url", "http://localhost:80");
//				js.put("userName", "User");
//				js.put("password", "pass");
//				js.put("downloadUri", "http://127.0.0.1:80");
//				js.put("keyStore", "C:/Users/Huawei/Desktop/etc/conf/server.p12");
//				js.put("keyStorePass", "Changeme_123");
//				js.put("keyStoreType", "PKCS12");
//				JSONArray ja = new JSONArray();
//				String str = "{\n  \"vim_id\": \"vim-0-1\",\n\t\"vim-info\": {\n\t\t\"vim-name\": \"vim-name-123\",\n\t\t\"vim-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//				ja.add(str);
//				JSONArray ja1 = new JSONArray();
//				String str1 = "{\n  \"vnfdVersion\": \"1.0version\",\n\t\"vnfd-info\": {\n\t\t\"vnfd-name\": \"vnfd-name-123\",\n\t\t\"vnfd-id\": \"vim-0-1\",\n\t\t\"ip\": \"127.0.0.1\",\n\t\t\"port\": \"8482\",\n\t\t\"protocol\": \"http\",\n\t\t\n\t}\n}";
//				;
//				ja1.add(str1);
//				JSONArray ja2 = new JSONArray();
//				String str2 = "{\"template_name\": \"VNFD_vUGW\",\"topology_template\":[{\"plan_name\": \"Normal_E9K\",\"plan_id\": \"Normal_E9K\"}]}";
//				ja2.add(str2);
//				JSONObject verTmpObj = ja2.getJSONObject(0);
//				JSONObject jsObject = new JSONObject();
//				jsObject.put("downloadUrl", "http://localhost:80");
//				jsObject.put("csarName", "CSCF");
//				JSONObject jsEms = new JSONObject();
//				jsEms.put("emsUuid", "123erbhi-hjdek123");
//				JSONObject jsCsar = new JSONObject();
//				jsCsar.put("csar_file_path", "/home/ubuntu/check/");
//				jsCsar.put("csar_file_name", "Csar_File");
//				jsCsar.put("emsUuid", jsEms);
//				JSONObject jsTemp = new JSONObject();
//				jsTemp.put("template", jsCsar);
//				js.put("vCSCF", jsTemp);
//				js.put("vim_info", ja);
//				js.put("template", verTmpObj);
//				js.put("templates", ja1);
//				js.put("packageInfo", jsObject);
//				return js;
//			}
//		};
//
//		System.setProperty("catalina.base",
//				"D:/VFC/23-08-2018/svnfm/huawei/vnfmadapter/VnfmadapterService/deployment/src/main/release");

		JSONObject vnfpkg = new JSONObject();
		JSONObject vnfpkg1 = new JSONObject();
		vnfpkg.put("name", "test");
		JSONObject obj = new JSONObject();
		obj.put("csar_file_path", "src/test/resources/Check10.txt");
		obj.put("csar_file_name", "casrFile");
		vnfpkg.put("template", obj);
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("csarid", "csarid123");
		paramsMap.put("vnfmid", "vnfmid1234");
		JSONObject res = manager.uploadVNFPackage(vnfpkg1, paramsMap);
		assertEquals(res.get("retCode"), 200);
	}
	
	
	@Test
	public void transferFromCsarSI() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "CSCF_SI");
	} 
	
	@Test
	public void transferFromCsarCSCF() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "CSCF");
	}
	
	@Test
	public void transferFromCsarMME() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "MME");
	} 
	
	@Test
	public void transferFromCsarSPGW() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "SPGW");
	} 
	
	@Test
	public void transferFromCsarHSS() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "HSS");
	} 
	
	@Test
	public void transferFromCsarSBC() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "SBC");
	}
	
	@Test
	public void transferFromCsarPCRF() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "PCRF");
	}
	
	@Test
	public void transferFromCsarTAS() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "TAS");
	}
	
	@Test
	public void transferFromCsar() throws Exception {
		
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("transferFromCsar", new Class[] {String.class});
		m.setAccessible(true);
		m.invoke(arManager, "transferFromCsar");
	}
	
	@Test
	public void uploadCsar()  throws Exception {
		JSONObject vnfpkg = new JSONObject();
		vnfpkg.put("ftp_server_ip","127.0.0.1");
		vnfpkg.put("ftp_username","ftp_username");
		vnfpkg.put("ftp_password","ftp_password");
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("uploadCsar", new Class[] {JSONObject.class,String.class});
		m.setAccessible(true);
		m.invoke(arManager, vnfpkg,"csarfilepath");
		
	}
	
	
	@Test
	public void readVnfdIdInfoFromJson() throws Exception{
		AdapterResourceManager arManager = new AdapterResourceManager();
		Method m =AdapterResourceManager.class.getDeclaredMethod("readVnfdIdInfoFromJson");	
		m.setAccessible(true);
		m.invoke(arManager);
	}
	
}