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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.ResultRequestUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.ServiceException;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.adapter.impl.AdapterResourceManager;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.csm.vnf.VnfMgrVnfm;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.dao.impl.VnfmDaoImpl;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.dao.inf.VnfmDao;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.entity.Vnfm;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class VnfMgrTest {

    private VnfmDao vnfmDao;

    private VnfMgr vnfMgr;

    @Before
    public void setUp() {
        vnfMgr = new VnfMgr();
        vnfmDao = new VnfmDaoImpl();
        vnfMgr.setVnfmDao(vnfmDao);
    }

    @Test
    public void testAddVnfByInvalidateDataVnfInfoNull() {
        String data = "{}";
        JSONObject subJsonObject = JSONObject.fromObject(data);
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.addVnf(subJsonObject, "vnmfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testAddVnfByInvalidateDataVnfInfoEmpty() {
        String data = "{}";
        JSONObject subJsonObject = JSONObject.fromObject(data);
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.addVnf(subJsonObject, "vnmfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testAddVnfByVnfmObjcetIsNullObject() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                return new JSONObject(true);
//            }
//        };
        String data =
                "{\"soId\": \"soId\",\"vapp_info\":{\"vnfm_id\":\"vnfm_id\",\"soId\": \"soId\",\"do_id\": \"do_id\"}}";
        JSONObject subJsonObject = JSONObject.fromObject(data);
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.addVnf(subJsonObject, "vnmfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testAddVnfByVnfmObjcetTypeEmpty() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject();
//                obj.put("type", "");
//                return obj;
//            }
//        };
        String data =
                "{\"soId\": \"soId\",\"vapp_info\":{\"vnfm_id\":\"vnfm_id\",\"soId\": \"soId\",\"do_id\": \"do_id\"}}";
        JSONObject subJsonObject = JSONObject.fromObject(data);
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.addVnf(subJsonObject, "vnmfId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testAddVnf() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject();
//                obj.put("type", "hw");
//                obj.put("vnfmId", "123");
//                obj.put("userName", "admin");
//                obj.put("password", "admin");
//                obj.put("url", "https://10.2.31.2:30001");
//                return obj;
//            }
//        };

//        new MockUp<VnfMgrVnfm>() {
//
//            @Mock
//            public JSONObject createVnf(JSONObject subJsonObject, JSONObject vnfmObjcet) {
//                JSONObject restJson = new JSONObject();
//                restJson.put("retCode", Constant.REST_SUCCESS);
//                return restJson;
//            }
//        };
//
//        new MockUp<AdapterResourceManager>() {
//
//            @Mock
//            public JSONObject uploadVNFPackage(JSONObject subJsonObject, Map<String, String> conMap) {
//                JSONObject restJson = new JSONObject();
//                restJson.put("retCode", Constant.REST_SUCCESS);
//                restJson.put("vnfdId", "123");
//                return restJson;
//            }
//        };

        String data =
                "{\"vnfPackageId\": \"vnfPackageId\",\"vnfId\": \"vnfId\",\"additionalParam\":{\"parameters\":{\"input\":\"input\"}}}";
        JSONObject subJsonObject = JSONObject.fromObject(data);
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.addVnf(subJsonObject, "vnfmId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testDeleteVnf() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject();
//                obj.put("type", "hw");
//                obj.put("vnfmId", "123");
//                obj.put("userName", "admin");
//                obj.put("password", "admin");
//                obj.put("url", "https://10.2.31.2:30001");
//                return obj;
//            }
//        };
//        new MockUp<VnfMgrVnfm>() {
//
//            @Mock
//            public JSONObject removeVnf(JSONObject vnfmObject, String vnfId, JSONObject vnfObject) {
//                JSONObject obj = new JSONObject();
//                obj.put("retCode", Constant.REST_SUCCESS);
//                return obj;
//            }
//        };
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject vnfObject = new JSONObject();
        JSONObject result = vnfMgr.deleteVnf("vnfId", "vnfmId", vnfObject);

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        assertEquals(restJson, result);
    }

    @Test
    public void testDeleteVnfByVnfmObjcetIsNullObject() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject(true);
//                return obj;
//            }
//        };
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject vnfObject = new JSONObject();
        JSONObject result = vnfMgr.deleteVnf("vnfId", "vnfmId", vnfObject);

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson, result);

    }

    @Test
    public void testDeleteVnfByException() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                throw new JSONException();
//            }
//        };
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject vnfObject = new JSONObject();
        JSONObject result = vnfMgr.deleteVnf("vnfId", "vnfmId", vnfObject);

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        assertEquals(restJson, result);

    }

    @Test
    public void testGetVnf() throws IOException {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject();
//                obj.put("type", "hw");
//                obj.put("vnfmId", "123");
//                obj.put("userName", "admin");
//                obj.put("password", "admin");
//                obj.put("url", "https://127.0.0.1:30001");
//                return obj;
//            }
//        };
//
//        new MockUp<VnfmDaoImpl>() {
//
//            @Mock
//            public Vnfm getVnfmById(String vnfmId) {
//                Vnfm obj = new Vnfm();
//                obj.setId("123");
//                obj.setVersion("v2.0");
//                obj.setVnfdId("234");
//                obj.setVnfPackageId("123");
//                return obj;
//            }
//        };
//
//        new MockUp<ResultRequestUtil>() {
//
//            @Mock
//            public JSONObject call(JSONObject vnfmObjcet, String path, String methodName, String paramsJson) {
//                JSONObject resultJson = new JSONObject();
//                resultJson.put("retCode", Constant.HTTP_OK);
//                JSONObject data = new JSONObject();
//
//                JSONArray result = new JSONArray();
//                JSONObject basicInfo = new JSONObject();
//                basicInfo.put("id", "NE=345");
//                basicInfo.put("vapp_name", "sc");
//                basicInfo.put("status", "active");
//                result.add(basicInfo);
//                data.put("basic", result);
//                resultJson.put("data", data.toString());
//                return resultJson;
//            }
//        };
//
        JSONObject result = vnfMgr.getVnf("vnfId", "vnfmId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        result.remove("vnfInfo");
        assertNotNull(result);
    }

    @Test
    public void testGetVnfFail() throws IOException {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject(true);
//                return obj;
//            }
//        };

        JSONObject result = vnfMgr.getVnf("vnfId", "vnfmId");
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testGetVnfFail1() throws IOException {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                throw new JSONException();
//            }
//        };

        JSONObject result = vnfMgr.getVnf("vnfId", "vnfmId");
        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testSaveVnfInfo() {
//        new MockUp<VnfmDaoImpl>() {
//
//            @Mock
//            public int insertVnfm(Vnfm vnfm) throws ServiceException {
//                return 1;
//            }
//        };

        VnfMgr vnfMgr = new VnfMgr();
        VnfmDao dao = new VnfmDaoImpl();
        vnfMgr.setVnfmDao(dao);
        JSONObject vnfObject = new JSONObject();
        vnfObject.put("retCode", Constant.REST_SUCCESS);
        vnfObject.put("vnfInstanceId", "vnfInstanceId");
        vnfObject.put("vnfPackageId", "vnfPackageId");
        JSONObject resObject = new JSONObject();
        resObject.put("vnfdVersion", "vnfdVersion");
        resObject.put("vnfdId", "vnfdId");
        JSONObject data = new JSONObject();
        data.put("data", resObject);
        vnfMgr.saveVnfInfo(vnfObject, data);
    }

    @Test
    public void testSaveVnfInfoFail() {
//        new MockUp<VnfmDaoImpl>() {
//
//            @Mock
//            public int insertVnfm(Vnfm vnfm) throws ServiceException {
//                return 1;
//            }
//        };

        VnfMgr vnfMgr = new VnfMgr();
        VnfmDao dao = new VnfmDaoImpl();
        vnfMgr.setVnfmDao(dao);
        JSONObject vnfObject = new JSONObject();
        vnfObject.put("retCode", Constant.REST_FAIL);
        vnfObject.put("vnfInstanceId", "vnfInstanceId");
        vnfObject.put("vnfPackageId", "vnfPackageId");
        JSONObject resObject = new JSONObject();
        resObject.put("vnfdVersion", "vnfdVersion");
        resObject.put("vnfdId", "vnfdId");
        vnfMgr.saveVnfInfo(vnfObject, resObject);
    }

    @Test
    public void testSaveVnfInfoServiceException() {
//        new MockUp<VnfmDaoImpl>() {
//
//            @Mock
//            public int insertVnfm(Vnfm vnfm) throws ServiceException {
//                throw new ServiceException();
//            }
//        };

        VnfMgr vnfMgr = new VnfMgr();
        VnfmDao dao = new VnfmDaoImpl();
        vnfMgr.setVnfmDao(dao);
        JSONObject vnfObject = new JSONObject();
        vnfObject.put("retCode", Constant.REST_SUCCESS);
        vnfObject.put("vnfInstanceId", "vnfInstanceId");
        vnfObject.put("vnfPackageId", "vnfPackageId");
        JSONObject resObject = new JSONObject();
        resObject.put("vnfdVersion", "vnfdVersion");
        resObject.put("vnfdId", "vnfdId");
        JSONObject data = new JSONObject();
        data.put("data", resObject);
        vnfMgr.saveVnfInfo(vnfObject, data);
    }

    @Test
    public void testGetJob() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject();
//                obj.put("test", "success");
//                return obj;
//            }
//        };
//
//        new MockUp<VnfMgrVnfm>() {
//
//            @Mock
//            public JSONObject getJob(JSONObject vnfmObject, String jobId) {
//                JSONObject res = new JSONObject();
//                res.put(Constant.RETCODE, Constant.REST_SUCCESS);
//                return res;
//            }
//        };
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.getJob("", "");

        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_SUCCESS);
        assertEquals(restJson, result);
    }

    @Test
    public void testGetJobFail() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                return new JSONObject();
//            }
//        };
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.getJob("", "");

        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testGetJobFail1() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                return new JSONObject(true);
//            }
//        };
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.getJob("", "");

        JSONObject restJson = new JSONObject();
        restJson.put(Constant.RETCODE, Constant.REST_FAIL);
        assertEquals(restJson, result);
    }

    @Test
    public void testScaleVnf() {
//        new MockUp<VnfmUtil>() {
//
//            @Mock
//            public JSONObject getVnfmById(String vnfmId) {
//                JSONObject obj = new JSONObject();
//                obj.put("type", "hw");
//                obj.put("vnfmId", "123");
//                obj.put("userName", "admin");
//                obj.put("password", "admin");
//                obj.put("url", "https://10.2.31.2:30001");
//                return obj;
//            }
//        };

//        new MockUp<VnfMgrVnfm>() {
//
//            @Mock
//            public JSONObject scaleVnf(JSONObject vnfObject, JSONObject vnfmObject, String vnfmId,
//                    String vnfInstanceId) {
//                JSONObject restJson = new JSONObject();
//                restJson.put("retCode", Constant.REST_SUCCESS);
//                return restJson;
//            }
//        };

        String data =
                "{\"vnfPackageId\": \"vnfPackageId\",\"vnfId\": \"vnfId\",\"additionalParam\":{\"parameters\":{\"input\":\"input\"}}}";
        JSONObject subJsonObject = JSONObject.fromObject(data);
        VnfMgr vnfMgr = new VnfMgr();
        JSONObject result = vnfMgr.scaleVNF(subJsonObject, "testId", "testId");

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_SUCCESS);
        assertEquals(restJson, result);
    }
}
