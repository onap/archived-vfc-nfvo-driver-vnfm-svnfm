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

package org.openo.nfvo.vnfmadapter.common;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openo.nfvo.vnfmadapter.service.csm.connect.ConnectMgrVnfm;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;

public class ResultRequestUtilTest {

    @Test
    public void callTestInternalError(){
        new MockUp<ConnectMgrVnfm>(){
            @Mock
            public int connect(JSONObject vnfmObj) {
                return 500;
            }
        };
        JSONObject vnfmObject = new JSONObject();;
        String path = "http://localhost:8080";
        String methodName = "get";
        String paramsJson = "";
        JSONObject resp = ResultRequestUtil.call(vnfmObject, path, methodName, paramsJson);
        assertTrue(resp.get("data").equals("connect fail."));
    }

    @Test
    public void callTestConnectionErrot(){
        new MockUp<ConnectMgrVnfm>(){
            @Mock
            public int connect(JSONObject vnfmObj) {
                return 200;
            }
        };
        JSONObject vnfmObject = new JSONObject();
        vnfmObject.put("url", "/test/123");
        String path = "http://localhost:8080";
        String methodName = "get";
        String paramsJson = "";
        JSONObject resp = ResultRequestUtil.call(vnfmObject, path, methodName, paramsJson);
        assertTrue(resp.get("data").equals("get connection error"));
    }

}
