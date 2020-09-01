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

package org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulResponse;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.constant.Constant;

import mockit.Mock;
import mockit.MockUp;
import net.sf.json.JSONObject;

public class AuthMgrTest {

    @Test
    public void testAuthTokenByDomainNameByJSONException() {
        AuthMgr authMgr = new AuthMgr();
        String data = "{\"auth\":{}}";
        JSONObject params = JSONObject.fromObject(data);

        JSONObject result = authMgr.authToken(params);

        JSONObject restJson = new JSONObject();
        restJson.put("retCode", Constant.REST_FAIL);
        restJson.put("data", "JSONException");
        assertEquals(restJson, result);
    }

    @Test
    public void testAuthToken() {

        AuthMgr authMgr = new AuthMgr();
        JSONObject params = new JSONObject();
        params.put(Constant.USERNAME, Constant.USERNAME);
        JSONObject result = authMgr.authToken(params);
        assertEquals(Constant.REST_SUCCESS, result.getInt("retCode"));
    }
}
