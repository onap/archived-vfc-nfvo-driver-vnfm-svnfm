/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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

import org.junit.Test;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.servicetoken.VnfmRestfulUtil;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.service.process.RegisterMgr;
import org.onap.vfc.nfvo.vnfm.svnfm.vnfmadapter.common.restclient.RestfulResponse;

import mockit.Mock;
import mockit.MockUp;

/**
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version VFC 1.0 Jan 13, 2017
 */
public class RegisterMgrTest {

    @Test
    public void testRegister() {
//        new MockUp<VnfmRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRemoteResponse(String url, String methodType, String params) {
//                RestfulResponse rsp = new RestfulResponse();
//                rsp.setStatus(200);
//                return rsp;
//            }
//        };
        RegisterMgr register = new RegisterMgr();
        register.register();



    }

    @Test
    public void testUnRegister() {
//        new MockUp<VnfmRestfulUtil>() {
//
//            @Mock
//            public RestfulResponse getRemoteResponse(String url, String methodType, String params) {
//                RestfulResponse rsp = new RestfulResponse();
//                rsp.setStatus(200);
//                return rsp;
//            }
//        };
        RegisterMgr register = new RegisterMgr();
        register.unRegister();
    }

}
