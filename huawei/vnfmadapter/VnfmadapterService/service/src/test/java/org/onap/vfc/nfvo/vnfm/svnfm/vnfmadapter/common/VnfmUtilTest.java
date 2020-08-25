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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.VnfmUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulResponse;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class VnfmUtilTest {

    @Test
    public void getVnfmByIdTestNullResp(){
        JSONObject resp = VnfmUtil.getVnfmById("1234");
        assertNull(resp);
    }

    @Test
    public void getVnfmByIdTestSuccess(){
//        new MockUp<VnfmRestfulUtil>(){
//            @Mock
//            public RestfulResponse getRemoteResponse(String url, String methodType, String params) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(200);
//                return resp;
//            }
//        };
        JSONObject resp = VnfmUtil.getVnfmById("1234");
        assertNotNull(resp);
    }

    @Test
    public void getVnfmIdByIpTestNullResp(){
        String resp = VnfmUtil.getVnfmIdByIp("localhost");
        assertTrue("".equals(resp));
    }

    @Test
    public void getVnfmIdByIpTestSuccess(){
//        new MockUp<VnfmRestfulUtil>(){
//            @Mock
//            public RestfulResponse getRemoteResponse(String url, String methodType, String params) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(200);
//                JSONArray respArray = new JSONArray();
//                JSONObject obj = new JSONObject();
//                obj.put("url", "localhost");
//                obj.put("vnfmId", "1234");
//                respArray.add(obj);
//                resp.setResponseJson(respArray.toString());
//                return resp;
//            }
//        };
        String resp = VnfmUtil.getVnfmIdByIp("localhost");
        assertTrue("1234".equals(resp));
    }

    @Test
    public void getVnfmIdByIpTestSuccessInvalidIP(){
//        new MockUp<VnfmRestfulUtil>(){
//            @Mock
//            public RestfulResponse getRemoteResponse(String url, String methodType, String params) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(200);
//                JSONArray respArray = new JSONArray();
//                JSONObject obj = new JSONObject();
//                obj.put("url", "127.0.0.1");
//                obj.put("vnfmId", "1234");
//                respArray.add(obj);
//                resp.setResponseJson(respArray.toString());
//                return resp;
//            }
//        };
        String resp = VnfmUtil.getVnfmIdByIp("localhost");
        assertTrue("".equals(resp));
    }
    @Test
    public void getVnfmIdByIpTestSuccessEmptyResp(){
//        new MockUp<VnfmRestfulUtil>(){
//            @Mock
//            public RestfulResponse getRemoteResponse(String url, String methodType, String params) {
//                RestfulResponse resp = new RestfulResponse();
//                resp.setStatus(200);
//                JSONArray respArray = new JSONArray();
//                resp.setResponseJson(respArray.toString());
//                return resp;
//            }
//        };
    	VnfmUtil.mockForTest("Vfnid");
        String resp = VnfmUtil.getVnfmIdByIp("localhost");
        assertTrue("".equals(resp));
    }

}
